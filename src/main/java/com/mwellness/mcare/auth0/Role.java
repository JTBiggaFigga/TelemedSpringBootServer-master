package com.mwellness.mcare.auth0;

/**
 * Created by dev01 on 3/28/17.
 */
public enum Role {
    ROLE_ATHLETE{
        public String toString() {
            return "athlete";
        }
    },
    ROLE_PATIENT{
        public String toString() {
            return "patient";
        }
    },
    ROLE_DOCTOR{
        public String toString() {
            return "doctor";
        }
    },
    ROLE_USERS_ADMIN {
        public String toString() {
            return "users-admin";
        }
    };

    public static Role toRole(final String roleStr) {
        switch (roleStr) {
            case "users-admin": {
                return ROLE_USERS_ADMIN;
            }
            case "athlete": {
                return ROLE_ATHLETE;
            }
            case "patient": {
                return ROLE_PATIENT;
            }
            case "doctor": {
                return ROLE_DOCTOR;
            }
            default:
                return null;
        }
    }
}
