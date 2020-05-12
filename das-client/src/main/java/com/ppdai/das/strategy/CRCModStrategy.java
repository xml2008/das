package com.ppdai.das.strategy;

/**
 * Mod CRC hash string
 */
public class CRCModStrategy extends AdvancedModStrategy {

    @Override
    protected ModShardLocator createLocator(int mod) {
        return new CRCModShardLocator(mod);
    }

}
