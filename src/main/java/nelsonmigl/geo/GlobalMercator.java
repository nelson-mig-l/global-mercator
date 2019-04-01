package nelsonmigl.geo;

public class GlobalMercator {

    private final int tileSize;
    private final double initialResolution;
    private final double originShift;

    public GlobalMercator() {
        this(256);
    }

    public GlobalMercator(final int tileSize) {
        this.tileSize = tileSize;
        this.initialResolution = 2 * Math.PI * 6378137 / this.tileSize;
        this.originShift = 2 * Math.PI * 6378137 / 2.0;
    }

    /**
     * Converts given lat/lon in WGS84 Datum to XY in Spherical Mercator EPSG:900913
     */
    public Coordinate latLonToMeters(final Coordinate latLon) {
        final double lat = latLon.x;
        final double lon = latLon.y;

        double mx = lon * originShift / 180.0;
        double my = Math.log(Math.tan((90 + lat) * Math.PI / 360.0)) / (Math.PI / 180.0);
        my = my * originShift / 180.0;
        return new Coordinate(mx, my);
    }

    /**
     * Converts XY point from Spherical Mercator EPSG:900913 to lat/lon in WGS84 Datum
     */
    public Coordinate metersToLatLon(final Coordinate m) {
        double lon = (m.x / originShift) * 180.0;
        double lat = (m.y / originShift) * 180.0;

        lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);
        return new Coordinate(lat, lon);
    }

    /**
     * Converts pixel coordinates in given zoom level of pyramid to EPSG:900913
     */
    public Coordinate pixelsToMeters(final Coordinate p, final int zoom) {
        final double res = resolution(zoom);
        double mx = p.x * res - originShift;
        double my = p.y * res - originShift;
        return new Coordinate(mx, my);
    }

    /**
     * Converts EPSG:900913 to pyramid pixel coordinates in given zoom level
     */
    public Coordinate metersToPixels(final Coordinate m, final int zoom) {
        final double res = resolution(zoom);
        double px = (m.x + originShift) / res;
        double py = (m.y + originShift) / res;
        return new Coordinate(px, py);
    }

    /**
     * Returns a tile covering region in given pixel coordinates
     */
    public Coordinate pixelsToTile(final Coordinate p) {
        int tx = (int) (Math.ceil(p.x / (double) (tileSize)) - 1);
        int ty = (int) (Math.ceil(p.y / (double) (tileSize)) - 1);
        return new Coordinate(tx, ty);
    }

    /**
     * Move the origin of pixel coordinates to top-left corner
     */
    public Coordinate pixelsToRaster(final Coordinate p, final int zoom) {
        final int mapSize = tileSize << zoom;
        return new Coordinate(p.x, mapSize - p.y);
    }

    /**
     * Returns tile for given mercator coordinates
     */
    public Coordinate metersToTile(Coordinate m, int zoom) {
        final Coordinate p = metersToPixels(m, zoom);
        return pixelsToTile(p);
    }

    /**
     * Returns bounds of the given tile in EPSG:900913 coordinates
     */
    public Envelope tileBounds(final Coordinate t, final int zoom) {
        final Coordinate min = pixelsToMeters(new Coordinate(t.x * tileSize, t.y * tileSize), zoom);
        final Coordinate max = pixelsToMeters(new Coordinate((t.x + 1) * tileSize, (t.y + 1) * tileSize), zoom);
        return new Envelope(min, max);
    }


    /**
     * Returns bounds of the given tile in latutude/longitude using WGS84 datum
     */
    public Envelope tileLatLonBounds(final Coordinate t, final int zoom) {
        final Envelope bounds = tileBounds(t, zoom);
        final Coordinate min = metersToLatLon(new Coordinate(bounds.getMinX(), bounds.getMinY()));
        final Coordinate max = metersToLatLon(new Coordinate(bounds.getMaxX(), bounds.getMaxY()));
        return new Envelope(min, max);
    }

    /**
     * Resolution (meters/pixel) for given zoom level (measured at Equator)
     */
    public double resolution(int zoom) {
        //return (2 * Math.PI * 6378137) / (this.tileSize * Math.pow(2, zoom));
        return initialResolution / Math.pow(2, zoom);
    }

    /**
     * Maximal scaledown zoom of the pyramid closest to the pixelSize.
     */
    public int zoomForPixelSize(final int pixelSize) {
        for (int i = 0; i < 30; i++) {
            if (pixelSize > resolution(i)) {
                if (i != 0) {
                    return i - 1;
                } else {
                    return 0; // We don 't want to scale up
                }
            }
        }
        throw new RuntimeException();
    }

    /**
     * Converts TMS tile coordinates to Google Tile coordinates
     */
    public Coordinate googleTile(final Coordinate t, final int zoom) {
        // coordinate origin is moved from bottom-left to top-left corner of the extent
        return new Coordinate(t.x, (Math.pow(2, zoom) - 1) - t.y);
    }

    /**
     * Converts TMS tile coordinates to Microsoft QuadTree
     */
    public String quadTree(final Coordinate t, final int zoom) {
        StringBuilder quadKey = new StringBuilder();
        int y = ((int) Math.pow(2, zoom) - 1) - (int) t.y;
        for (int i = zoom; i > 0; i--) {
            int digit = 0;
            int mask = 1 << (i - 1);
            if (((int) t.x & mask) != 0) {
                digit += 1;
            }
            if ((y & mask) != 0) {
                digit += 2;
            }
            quadKey.append(digit);
        }

        return quadKey.toString();
    }

}
