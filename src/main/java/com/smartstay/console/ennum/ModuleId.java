package com.smartstay.console.ennum;

public enum ModuleId {
    Hostels(1),
    Tenants(2),
    Subscriptions(3),
    Plans(4),
    Invoices(5),
    Hostel_Bank_Transactions(6),
    Assets(7),
    Amenities(8),
    Cities(9),
    States(10),
    Agents(11),
    Updates(12),
    Agreements(13);




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
