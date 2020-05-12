package com.ppdai.das.strategy;

/**
 * Mod MD5 hash string
 */
public class HashModStrategy extends AdvancedModStrategy  {

    @Override
    protected ModShardLocator createLocator(int mod) {
        return new HashModShardLocator(mod);
    }

}
