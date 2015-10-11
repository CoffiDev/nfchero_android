package cuenca.alejandro.com.nfcgame.Models;

/**
 * Created by alejandro on 10/10/15.
 */
public class Player {

    private String health;
    private String damage;
    private CardWeapon weapon;

    public String getDamage() {
        return damage;
    }

    public String getHealth() {
        return health;
    }

    public CardWeapon getWeapon() {
        return weapon;
    }

    public Player(String health, String damage, CardWeapon weapon) {
        this.health = health;
        this.damage = damage;
        this.weapon = weapon;
    }
}
