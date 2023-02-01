package net.stickmix.prisonevo.boss;

import lombok.Data;
import lombok.val;

import java.util.concurrent.ThreadLocalRandom;

@Data
public abstract class BossSpell {

    private final int cooldown;
    private final int cooldownHeroic;
    private long lastUsage = 0L;

    public abstract void process();

    public void cast(BossScript script) {
        int cooldown = script.isHeroic() ? this.cooldownHeroic : this.cooldown;
        long current = System.currentTimeMillis();
        val i = ThreadLocalRandom.current().nextInt(100);
        if (current - lastUsage < cooldown * 1000L || i != 7) {
            return;
        }
        lastUsage = current;
        process();
    }

}
