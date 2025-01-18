package org.dreeam.leaf.util.math.random;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.BitRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.dreeam.leaf.config.modules.opt.FastRNG;

import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;


public class FasterRandomSource implements BitRandomSource {
    private static final int INT_BITS = 48;
    private static final long SEED_MASK = 0xFFFFFFFFFFFFL;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private static final RandomGeneratorFactory<RandomGenerator> RANDOM_GENERATOR_FACTORY = RandomGeneratorFactory.of(FastRNG.randomGenerator);
    private static final boolean isSplittableGenerator = RANDOM_GENERATOR_FACTORY.isSplittable();
    private long seed;
    private RandomGenerator randomGenerator;
    public static final FasterRandomSource SHARED_INSTANCE = new FasterRandomSource(ThreadLocalRandom.current().nextLong());

    public FasterRandomSource(long seed) {
        this.seed = seed;
        this.randomGenerator = RANDOM_GENERATOR_FACTORY.create(seed);
    }

    private FasterRandomSource(long seed, RandomGenerator.SplittableGenerator randomGenerator) {
        this.seed = seed;
        this.randomGenerator = randomGenerator;
    }

    @Override
    public final RandomSource fork() {
        if (isSplittableGenerator) {
            return new FasterRandomSource(seed, ((RandomGenerator.SplittableGenerator) this.randomGenerator).split());
        }
        return new FasterRandomSource(this.nextLong());
    }

    @Override
    public final PositionalRandomFactory forkPositional() {
        return new FasterRandomSourcePositionalRandomFactory(this.seed);
    }

    @Override
    public final void setSeed(long seed) {
        this.seed = seed;
        this.randomGenerator = RANDOM_GENERATOR_FACTORY.create(seed);
    }

    @Override
    public final int next(int bits) {
        // >>> instead of Mojang's >> fixes MC-239059
        return (int) ((seed * MULTIPLIER + INCREMENT & SEED_MASK) >>> INT_BITS - bits);
    }

    public static class FasterRandomSourcePositionalRandomFactory implements PositionalRandomFactory {
        private final long seed;

        public FasterRandomSourcePositionalRandomFactory(long seed) {
            this.seed = seed;
        }

        @Override
        public RandomSource at(int x, int y, int z) {
            long l = Mth.getSeed(x, y, z);
            long m = l ^ this.seed;
            return new FasterRandomSource(m);
        }

        @Override
        public RandomSource fromHashOf(String seed) {
            int i = seed.hashCode();
            return new FasterRandomSource((long)i ^ this.seed);
        }

        @Override
        public RandomSource fromSeed(long seed) {
            return new FasterRandomSource(seed);
        }

        @VisibleForTesting
        @Override
        public void parityConfigString(StringBuilder info) {
            info.append("FasterRandomSourcePositionalRandomFactory{").append(this.seed).append("}");
        }
    }

    @Override
    public final int nextInt() {
        return randomGenerator.nextInt();
    }

    @Override
    public final int nextInt(int bound) {
        return randomGenerator.nextInt(bound);
    }

    @Override
    public final long nextLong() {
        return randomGenerator.nextLong();
    }

    @Override
    public final boolean nextBoolean() {
        return randomGenerator.nextBoolean();
    }

    @Override
    public final float nextFloat() {
        return randomGenerator.nextFloat();
    }

    @Override
    public final double nextDouble() {
        return randomGenerator.nextDouble();
    }

    @Override
    public final double nextGaussian() {
        return randomGenerator.nextGaussian();
    }
}
