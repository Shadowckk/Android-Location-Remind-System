package com.shadowckk.remind.DB;

import android.provider.BaseColumns;

public class DBContruct {
    public static final class TaskDataEntry implements BaseColumns {
        public static final String TABLE_NAME = "taskdata";
        public static final String COLUMN_TASK_DATA_STRING = "taskDataString";
        public static final String COLUMN_TYPE = "type";    //0-时间  1-进入  2-离开
        public static final String COLUMN_TIME_DATA_STRING = "taskTimeDataString";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_LOC_LATITUDE = "destLatitude";
        public static final String COLUMN_LOC_LONGITUDE = "destLongitude";
        public static final String COLUMN_LOC_RADIUS = "destRadius";
        public static final String COLUMN_LOC_NAME = "destName";

    }
    public static final class TaskedDataEntry implements BaseColumns {
        public static final String TABLE_NAME = "taskeddata";
        public static final String COLUMN_TASK_DATA_STRING = "taskDataString";
        public static final String COLUMN_TYPE = "type";    //0-时间  1-进入  2-离开
        public static final String COLUMN_TIME_DATA_STRING = "taskTimeDataString";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_LOC_LATITUDE = "destLatitude";
        public static final String COLUMN_LOC_LONGITUDE = "destLongitude";
        public static final String COLUMN_LOC_RADIUS = "destRadius";
        public static final String COLUMN_LOC_NAME = "destName";
    }
}