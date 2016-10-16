package com.example.mike.tasklist.database;

public class TaskDbSchema {
    public static final class TaskTable {
        public static final String NAME = "tasks";

        public static final class Cols{
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DESCRIPTION = "description";
            public static final String DATE = "date";
            public static final String REMINDER = "reminder";
            public static final String HAS_REMINDER = "has_reminder";
        }
    }
}