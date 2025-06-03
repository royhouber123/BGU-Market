package market.domain.Role;

public enum Permission {
    VIEW_ONLY(0),
    EDIT_PRODUCTS(1),
    EDIT_POLICIES(2),
    BID_APPROVAL(3);

    private final int code;

    Permission(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // Reverse lookup
    public static Permission fromCode(int code) throws IllegalArgumentException {
        for (Permission p : Permission.values()) {
            if (p.getCode() == code) {
                return p;
            }
        }
        throw new IllegalArgumentException("Invalid permission code: " + code);
    }
}
