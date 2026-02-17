package com.smartstay.console.ennum;

public enum ModuleId {
    Hostels(1),
    Tenants(2),
    Subscriptions(3),
    Plans(4),
    Invoices(5),
    Agents(6),
    Assets(7),
    Updates(8),
    Agreements(9),
    Amenities(10),
    Hostel_Bank_Transactions(11),
    Cities(12),
    States(13),
    Owners(14);


    private final int id;

    ModuleId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ModuleId fromId(int id) {
        for (ModuleId module : values()) {
            if (module.id == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid ModuleId: " + id);
    }
}
