package net.ghostrealms.econo.economy.economable;

/**
 * Created by River on 8/14/2016.
 */

public class EconomableTown implements Economable {

    private final String townUniqueId;

    public EconomableTown(String townUnique) {
        townUniqueId = townUnique;
    }

    @Override
    public String getUniqueIdentifier() {
        return "town:" + townUniqueId;
    }
}
