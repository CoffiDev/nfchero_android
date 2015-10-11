package cuenca.alejandro.com.nfcgame.Models;

/**
 * Created by alejandro on 10/10/15.
 */
public class CardMonster {

    private String type;
    private String health;
    private String damage;

    private CardWeapon weapon;

    public CardMonster(String type, String health, CardWeapon weapon, String damage) {
        this.type = type;
        this.health = health;
        this.weapon = weapon;
        this.damage = damage;
    }
}
