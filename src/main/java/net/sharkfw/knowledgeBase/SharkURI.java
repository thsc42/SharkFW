package net.sharkfw.knowledgeBase;

import net.sharkfw.asip.ASIPSpace;

/**
 * @author thsc
 */
public class SharkURI {
    public static final String SHARK_URI_DOMAIN = "shark://";

    public static final String TIME_PREFIX = SHARK_URI_DOMAIN + "time;";

    public static final String TIME_FROM_TAG = "from";
    public static final String TIME_DURATION_TAG = "duration";
    public static final String TAG_SEPARATOR = ",";


    public static String timeST(long from, long duration) {
        /**
         * shark://time;<from>,<to>
         */

        StringBuilder buf = new StringBuilder();

        buf.append(SHARK_URI_DOMAIN);

        buf.append(TIME_FROM_TAG);
        buf.append(String.valueOf(from));

        buf.append(TAG_SEPARATOR);

        buf.append(TIME_DURATION_TAG);
        buf.append(String.valueOf(duration));

        return buf.toString();
    }

    public static String geoST() {
        return "dummyGeoSI";
    }

    /**
     * return si describing this direction. NOTHING is taken as
     * default even if a parameter is used that isn't a known constant in
     * ContextSpace
     *
     * @param direction
     * @return
     */
    public static String getDirectionSI(int direction) {
        switch (direction) {
            case ASIPSpace.DIRECTION_IN:
                return ASIPSpace.INURL;
            case ASIPSpace.DIRECTION_OUT:
                return ASIPSpace.OUTURL;
            case ASIPSpace.DIRECTION_INOUT:
                return ASIPSpace.INOUTURL;
            default:
                return ASIPSpace.NO_DIRECTION_URL;
        }
    }
}
