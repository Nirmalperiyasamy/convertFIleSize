package com.hriday.convertFileSize.utils;

public enum ErrorMessage {
    MAXIMUM_SIZE {
        public String toString() {
            return "The file size exceeds the maximum allowed limit.";
        }
    },
    TYPE_NOT_FOUND {
        public String toString() {
            return "Type not found";
        }
    },
    FILE_NOT_EXIST {
        public String toString() {
            return "the file doesn't exist or not readable";
        }
    },
    NOT_READABLE {
        public String toString() {
            return "Issue in reading the file";
        }
    },
    EXCEPTION_ZIP {
        public String toString() {
            return "some exception while zip";
        }
    },

}
