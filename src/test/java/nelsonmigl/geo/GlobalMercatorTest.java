package nelsonmigl.geo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tested against python version.
 * https://repl.it/repls/FunnyIndigoOpendoc
 */
public class GlobalMercatorTest {
    public static final double DELTA = 1.0E-8;
    public static final Coordinate ZERO = new Coordinate(0, 0);
    public static final Coordinate LISBON = new Coordinate(38.716667, -9.133333);
    public static final Coordinate FALKLAND = new Coordinate(-51.75, -59);
    public static final Coordinate WARSAW = new Coordinate(52.233333, 21.016667);

    private GlobalMercator gm;

    @Before
    public void setup() {
        gm = new GlobalMercator();
    }

    @Test
    public void shouldLatLonToMetersAndMetersToLatLonForSelectedLocations() {
        final Coordinate zero = gm.latLonToMeters(ZERO);
        assertCoordinate(zero, 0, 0);
        assertCoordinate(gm.metersToLatLon(zero), ZERO);

        final Coordinate lisbon = gm.latLonToMeters(LISBON);
        assertCoordinate(lisbon, -1016717.9788054017, 4681167.431057864);
        assertCoordinate(gm.metersToLatLon(lisbon), LISBON);

        final Coordinate falkland = gm.latLonToMeters(FALKLAND);
        assertCoordinate(falkland, -6567849.956803142, -6755047.863034452);
        assertCoordinate(gm.metersToLatLon(falkland), FALKLAND);

        final Coordinate warsaw = gm.latLonToMeters(WARSAW);
        assertCoordinate(warsaw, 2339564.6686117966, 6842425.510386324);
        assertCoordinate(gm.metersToLatLon(warsaw), WARSAW);
    }

    @Test
    public void shouldReturnSameCoordinateForZeroZeroAtAllZooms() {
        for (int zoom = 0; zoom < 22; zoom++) {
            final Coordinate zero = gm.pixelsToMeters(ZERO, 1);
            assertCoordinate(zero, -20037508.342789244, -20037508.342789244);
            assertCoordinate(gm.metersToPixels(zero, 1), ZERO);
        }
    }

    @Test
    public void shouldPixelsToMetersAndMetersToPixelsForSelectedLocations() {
        final Coordinate warsaw1 = gm.pixelsToMeters(WARSAW, 1);
        assertCoordinate(warsaw1, -15949126.132792413, -18392501.935171574);
        assertCoordinate(gm.metersToPixels(warsaw1, 1), WARSAW);

        final Coordinate warsaw11 = gm.pixelsToMeters(WARSAW, 11);
        assertCoordinate(warsaw11, -20033515.782037295, -20035901.891219303);
        assertCoordinate(gm.metersToPixels(warsaw11, 11), WARSAW);

        final Coordinate falkland5 = gm.pixelsToMeters(FALKLAND, 5);
        assertCoordinate(falkland5, -20290667.78046975, -20326134.56159407);
        assertCoordinate(gm.metersToPixels(falkland5, 5), FALKLAND);

        final Coordinate falkland18 = gm.pixelsToMeters(FALKLAND, 18);
        assertCoordinate(falkland18, -20037539.246040914, -20037543.57548197);
        assertCoordinate(gm.metersToPixels(falkland18, 18), FALKLAND);

        final Coordinate lisbon12 = gm.pixelsToMeters(LISBON, 12);
        assertCoordinate(lisbon12, -20036028.64930395, -20037857.405205674);
        assertCoordinate(gm.metersToPixels(lisbon12, 12), LISBON);
    }

    @Test
    public void testPixelsToTile() {
        final Coordinate zero = gm.pixelsToTile(new Coordinate(0, 0));
        assertCoordinate(zero, new Coordinate(-1, -1));

        final Coordinate ur = gm.pixelsToTile(new Coordinate(0.5, 0.5));
        assertCoordinate(ur, new Coordinate(0, 0));
        final Coordinate lr = gm.pixelsToTile(new Coordinate(0.5, -0.5));
        assertCoordinate(lr, new Coordinate(0, -1));
        final Coordinate ll = gm.pixelsToTile(new Coordinate(-0.5, -0.5));
        assertCoordinate(ll, new Coordinate(-1, -1));
        final Coordinate ul = gm.pixelsToTile(new Coordinate(-0.5, 0.5));
        assertCoordinate(ul, new Coordinate(-1, 0));

        final Coordinate oo = gm.pixelsToTile(new Coordinate(256, 256));
        assertCoordinate(oo, new Coordinate(0, 0));
        final Coordinate ox = gm.pixelsToTile(new Coordinate(257, 256));
        assertCoordinate(ox, new Coordinate(1, 0));
        final Coordinate oy = gm.pixelsToTile(new Coordinate(256, 257));
        assertCoordinate(oy, new Coordinate(0, 1));
    }

    @Test
    public void testPixelsToRaster() {
        assertCoordinate(gm.pixelsToRaster(new Coordinate(1000, 1000), 2), 1000, 24);
        assertCoordinate(gm.pixelsToRaster(new Coordinate(2000, -2000), 7), 2000, 34768);
        assertCoordinate(gm.pixelsToRaster(new Coordinate(-2222, 0), 14), -2222, 4194304);
    }

    @Test
    public void testMetersToTile() {
        assertCoordinate(gm.metersToTile(new Coordinate(10200, 25000), 16), 32784, 32808);
    }

    @Test
    public void testTileBoundsAtSelectedLocations() {
        final Coordinate zero15 = gm.metersToTile(gm.latLonToMeters(ZERO), 15);
        final Envelope zeroBounds = gm.tileBounds(zero15, 15);
        assertEnvelopeLowerLeft(zeroBounds, -1222.9924525618553, -1222.9924525618553);
        assertEnvelopeUpperRight(zeroBounds, 0.0, 0.0);

        final Coordinate warsaw6 = gm.metersToTile(gm.latLonToMeters(WARSAW), 6);
        final Envelope warsawBounds = gm.tileBounds(warsaw6, 6);
        assertEnvelopeLowerLeft(warsawBounds, 1878516.4071364924, 6261721.357121639);
        assertEnvelopeUpperRight(warsawBounds, 2504688.542848654, 6887893.492833804);
    }

    @Test
    public void testTileBoundsAtMultipleLevels() {
        final Coordinate coordinate = gm.latLonToMeters(FALKLAND);
        for (int zoom = 0; zoom < 22; zoom++) {
            final Coordinate tile = gm.metersToTile(coordinate, zoom);
            final Envelope bounds = gm.tileBounds(tile, zoom);
            Assert.assertTrue(bounds.contains(coordinate));
        }
    }


    @Test
    public void testTileBoundsLatLonAtSelectedLocations() {
        final Coordinate zero15 = gm.metersToTile(gm.latLonToMeters(ZERO), 15);
        final Envelope zeroBounds = gm.tileLatLonBounds(zero15, 15);
        assertEnvelopeLowerLeft(zeroBounds, -0.010986328057668812, -0.010986328124991333);
        assertEnvelopeUpperRight(zeroBounds, 0.0, 0.0);

        final Coordinate warsaw6 = gm.metersToTile(gm.latLonToMeters(WARSAW), 6);
        final Envelope warsawBounds = gm.tileLatLonBounds(warsaw6, 6);
        assertEnvelopeLowerLeft(warsawBounds, 48.922499263758255, 16.875000000000007);
        assertEnvelopeUpperRight(warsawBounds, 52.482780222078226, 22.499999999999986);
    }

    @Test
    public void testTileBoundsLatLonAtMultipleLevels() {
        final Coordinate coordinate = gm.latLonToMeters(LISBON);
        for (int zoom = 0; zoom < 22; zoom++) {
            final Coordinate tile = gm.metersToTile(coordinate, zoom);
            final Envelope bounds = gm.tileLatLonBounds(tile, zoom);
            Assert.assertTrue(bounds.contains(LISBON));
        }
    }

    @Test
    public void testResolution() {
        Assert.assertEquals(4.777314267823516, gm.resolution(15), DELTA);
        Assert.assertEquals(152.8740565703525, gm.resolution(10), DELTA);
        Assert.assertEquals(4891.96981025128, gm.resolution(5), DELTA);
    }

    @Test
    public void testZoomForPixelSize() {
        Assert.assertEquals(7, gm.zoomForPixelSize(1222));
        Assert.assertEquals(6, gm.zoomForPixelSize(1223));
        Assert.assertEquals(10, gm.zoomForPixelSize(100));
    }

    @Test
    public void testGoogleTileForSelectedLocations() {
        final Coordinate warsawTms = gm.metersToTile(gm.latLonToMeters(WARSAW), 7);
        assertCoordinate(warsawTms, 71, 85);
        final Coordinate warsawGoogle = gm.googleTile(warsawTms, 7);
        assertCoordinate(warsawGoogle, 71, 42);

        final Coordinate lisbonTms = gm.metersToTile(gm.latLonToMeters(LISBON), 7);
        assertCoordinate(lisbonTms, 60, 78);
        final Coordinate lisbonGoogle = gm.googleTile(lisbonTms, 7);
        assertCoordinate(lisbonGoogle, 60, 49);

        final Coordinate falklandTms = gm.metersToTile(gm.latLonToMeters(FALKLAND), 7);
        assertCoordinate(falklandTms, 43, 42);
        final Coordinate falklandGoogle = gm.googleTile(falklandTms, 7);
        assertCoordinate(falklandGoogle, 43, 85);

        final Coordinate zeroTms = gm.metersToTile(gm.latLonToMeters(ZERO), 7);
        assertCoordinate(zeroTms, 63, 63);
        final Coordinate zeroGoogle = gm.googleTile(zeroTms, 7);
        assertCoordinate(zeroGoogle, 63, 64);
    }

    @Test
    public void testQuadTreeAtMultipleZoomLevels() {
        final Coordinate coordinate = gm.latLonToMeters(LISBON);
        final String expected = "033110211002200121232";
        for (int zoom = 0; zoom < 22; zoom++) {
            final Coordinate tms = gm.metersToTile(coordinate, zoom);
            final String key = gm.quadTree(tms, zoom);
            Assert.assertEquals(expected.substring(0, zoom), key);
        }
    }

    private void assertCoordinate(final Coordinate actual, final double x, final double y) {
        Assert.assertEquals(x, actual.x, DELTA);
        Assert.assertEquals(y, actual.y, DELTA);
    }

    private void assertCoordinate(final Coordinate actual, final Coordinate expected) {
        Assert.assertEquals(expected.x, actual.x, DELTA);
        Assert.assertEquals(expected.y, actual.y, DELTA);
    }

    private void assertEnvelopeLowerLeft(final Envelope actual, final double minX, final double minY) {
        Assert.assertEquals(minX, actual.getMinX(), DELTA);
        Assert.assertEquals(minY, actual.getMinY(), DELTA);
    }

    private void assertEnvelopeUpperRight(final Envelope actual, final double maxX, final double maxY) {
        Assert.assertEquals(maxX, actual.getMaxX(), DELTA);
        Assert.assertEquals(maxY, actual.getMaxY(), DELTA);
    }

}
