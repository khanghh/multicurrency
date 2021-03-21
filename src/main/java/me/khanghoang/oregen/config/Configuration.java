package me.khanghoang.oregen.config;

import me.khanghoang.oregen.OreBlock;
import me.khanghoang.oregen.OreGenerator;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.*;

/**
 * @author khanghh on 2021/02/15
 */
public class Configuration extends YamlConfig {

    private boolean debug;

    public static Configuration loadFromFile(File file) {
        Configuration config = new Configuration(file);
        config.load();
        config.applyConfig();
        return config;
    }

    public Configuration(File file) {
        super(file);
    }

    public void reload() {
        this.load();
        this.applyConfig();
    }

    private void applyConfig() {
        this.debug = this.getBoolean("debug");
    }

    public boolean isDebug() {
        return debug;
    }

    public void toggleDebug() {
        debug = !debug;
        this.set("debug", debug);
        this.save();
    }

    public void migrate(Configuration oldConfig) {
        for (String key : oldConfig.getKeys(false)) {
            if (this.contains(key)) {
                this.set(key, oldConfig.get(key));
            }
        }
        applyConfig();
    }
    
    public List<OreGenerator> getGenerators() {
        List<OreGenerator> generators = new ArrayList<>();
        ConfigurationSection genCfg = this.getConfigurationSection("generators");
        if (genCfg == null) return generators;
        for (String name : genCfg.getKeys(false)) {
            String item = genCfg.getString(name + ".item");
            String label = genCfg.getString(name + ".label");
            String symbol = genCfg.getString(name + ".symbol");
            int islandLevel = genCfg.getInt(name + ".islandLevel", 0);
            List<OreBlock> blocks = new ArrayList<>();
            ConfigurationSection blocksCfg = genCfg.getConfigurationSection(name + ".blocks");
            if (blocksCfg != null) {
                for (String blockName : blocksCfg.getKeys(false)) {
                    double chance = blocksCfg.getDouble(blockName, 0.0);
                    blocks.add(new OreBlock(blockName, chance));
                }
            }
            int rank = genCfg.getInt(name + ".rank", -1);
            OreGenerator generator = new OreGenerator(name, item, label, symbol, islandLevel, blocks, rank);
            generator.isDefault =  genCfg.getBoolean(name + ".default", false) || name.equals("default");
            generators.add(generator);
        }
        return generators;
    }

    public void setGenerators(List<OreGenerator> generators) {
        this.set("generators", null);
        for (OreGenerator generator : generators) {
            String name = generator.name ;
            this.set("generators." + name + ".item", generator.item);
            this.set("generators." + name + ".label", generator.label);
            this.set("generators." + name + ".symbol", generator.symbol);
            this.set("generators." + name + ".islandLevel", generator.islandLevel);
            this.set("generators." + name + ".blocks", new String[0]);
            this.set("generators." + name + ".rank", generator.rank);
            for (OreBlock block : generator.blocks) {
                this.set("generators." + name + ".blocks." + block.name, block.chance);
            }
            if (generator.isDefault && !name.equals("default")) {
                this.set("generators." + name + ".default", true);
            }
        }
        this.save();
    }

    public List<String> getDisabledWorlds() {
        return this.getStringList("disabledWorlds");
    }

    public void setDisabledWorlds(List<String> disabledWorlds) {
        this.set("disabledWorlds", disabledWorlds);
        this.save();
    }
}
