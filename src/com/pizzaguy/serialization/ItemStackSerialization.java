package com.pizzaguy.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.minecraft.server.v1_8_R3.NBTTagCompound;

public class ItemStackSerialization {
    // basic values
    public static final byte[] MATERIAL = "MA".getBytes();
    public static final byte[] AMOUNT = "AM".getBytes();
    public static final byte[] DURABILITY = "DU".getBytes();
    public static final byte[] MATERIALDATA = "MD".getBytes();

    // item meta / nbt tag values
    public static final byte[] ENCHANTS = "EN".getBytes();
    public static final byte[] DISPLAYNAME = "DN".getBytes();
    public static final byte[] LORE = "LO".getBytes();
    public static final byte[] ITEMFLAGS = "IF".getBytes();
    public static final byte[] BANNERMETA = "BA".getBytes();
    public static final byte[] BOOKMETA = "BO".getBytes();
    public static final byte[] ENCHANTMENTSTORAGEMETA = "EM".getBytes();
    public static final byte[] FIREWORKMETA = "FW".getBytes();
    public static final byte[] LEATHERARMORMETA = "LA".getBytes();
    public static final byte[] MAPMETA = "MM".getBytes();
    public static final byte[] POTIONMETA = "PM".getBytes();
    public static final byte[] SKULLMETA = "SM".getBytes();

    @SuppressWarnings("deprecation")
    public static byte[] serialize(ItemStack item) {
        //// Visible values ////
        // material //
        byte[] material = serializeMaterial(item.getType());
        // amount //
        byte[] amount = serializeAmount(item.getAmount());
        // durability //
        byte[] durability = serializeDurability(item.getDurability());
        // material data //
        byte[] materialData = serializeMaterialData(item.getData().getData());
        // enchants //
        byte[] enchants = serializeEnchantments(item.getEnchantments());

        //// base meta data ////
        ItemMeta meta = item.getItemMeta();
        // display name //
        byte[] displayname = meta.hasDisplayName() ? serializeDisplayName(meta.getDisplayName()) : null;
        // lore //
        byte[] lore = meta.hasLore() ? serializeLore(meta.getLore()) : null;
        // item flags //
        byte[] itemflags = serializeItemFlags(item.getItemMeta().getItemFlags());

        //// specified meta data ////
        byte[] metaData = null;
        // NBT tag for if for values that spigot does not support well
        net.minecraft.server.v1_8_R3.ItemStack stack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = stack.hasTag() ? stack.getTag() : new NBTTagCompound();
        if (meta instanceof BannerMeta)
            metaData = serializeBannerMeta((BannerMeta) meta);
        else if (meta instanceof BookMeta)
            metaData = serializeBookMeta((BookMeta) meta);
        else if (meta instanceof EnchantmentStorageMeta)
            metaData = serializeEnchantmentStorageMeta((EnchantmentStorageMeta) meta);
        else if (meta instanceof FireworkMeta)
            metaData = serializeFireworkMeta((FireworkMeta) meta);
        else if (meta instanceof LeatherArmorMeta)
            metaData = serializeLeatherArmorMeta((LeatherArmorMeta) meta);
        else if (meta instanceof MapMeta)
            metaData = serializeMapMeta((MapMeta) meta);
        else if (meta instanceof PotionMeta)
            metaData = serializePotionMeta((PotionMeta) meta);
        else if (meta instanceof SkullMeta)
            metaData = serializeSkullMeta((SkullMeta) meta);

        // build final byte array
        final byte[] data = new ByteArrayBuilder(material).add(amount).add(durability).add(materialData).add(enchants)
                .add(displayname).add(lore).add(itemflags).add(metaData).Build();
        return data;
    }

    @SuppressWarnings("deprecation")
    private static final byte[] serializeMaterial(Material material) {
        byte[] data = new byte[4];
        int pointer = SerializationWriter.writeBytes(0, data, MATERIAL);
        pointer = SerializationWriter.writeBytes(pointer, data, (short) material.getId());
        return data;
    }

    private static final byte[] serializeAmount(int amount) {
        byte[] data = new byte[4];
        int pointer = SerializationWriter.writeBytes(0, data, AMOUNT);
        pointer = SerializationWriter.writeBytes(pointer, data, (short) amount);
        return data;
    }

    private static final byte[] serializeDurability(short durability) {
        byte[] data = new byte[4];
        int pointer = SerializationWriter.writeBytes(0, data, DURABILITY);
        pointer = SerializationWriter.writeBytes(pointer, data, (short) durability);
        return data;
    }

    private static final byte[] serializeMaterialData(byte materialData) {
        byte[] data = new byte[3];
        int pointer = SerializationWriter.writeBytes(0, data, MATERIALDATA);
        pointer = SerializationWriter.writeBytes(pointer, data, materialData);
        return data;
    }

    @SuppressWarnings("deprecation")
    private static final byte[] serializeEnchantments(Map<Enchantment, Integer> enchants) {
        byte[] data = new byte[4 + (enchants.size() * 4)];
        int pointer = SerializationWriter.writeBytes(0, data, ENCHANTS);
        pointer = SerializationWriter.writeBytes(pointer, data, (short) enchants.size());
        for (Enchantment enchant : enchants.keySet()) {
            pointer = SerializationWriter.writeBytes(pointer, data, (short) enchant.getId());
            pointer = SerializationWriter.writeBytes(pointer, data, (short) enchants.get(enchant).shortValue());
        }
        return data;
    }

    private static final byte[] serializeDisplayName(String name) {
        byte[] data = new byte[4 + name.length()];
        int pointer = SerializationWriter.writeBytes(0, data, DISPLAYNAME);
        pointer = SerializationWriter.writeBytes(pointer, data, name);
        return data;
    }

    private static final byte[] serializeLore(List<String> lore) {
        int size = LORE.length + Short.BYTES;
        for (String line : lore)
            size += Short.BYTES + line.length();
        byte[] data = new byte[size];
        int pointer = SerializationWriter.writeBytes(0, data, LORE);
        pointer = SerializationWriter.writeBytes(pointer, data, (short) lore.size());
        for (String line : lore)
            pointer = SerializationWriter.writeBytes(pointer, data, line);
        return data;
    }

    private static final byte[] serializeItemFlags(Set<ItemFlag> set) {
        byte[] data = new byte[4 + set.size() * 2];
        int pointer = SerializationWriter.writeBytes(0, data, ITEMFLAGS);
        pointer = SerializationWriter.writeBytes(pointer, data, (short) set.size());
        for (ItemFlag flag : set)
            pointer = SerializationWriter.writeBytes(pointer, data, (short) flag.ordinal());
        return data;
    }

    private static final byte[] serializeBannerMeta(BannerMeta bMeta) {
        byte[] data = new byte[BANNERMETA.length + Short.BYTES * 2 + bMeta.getPatterns().size() * 4];
        int pointer = SerializationWriter.writeBytes(0, data, BANNERMETA);
        if (bMeta.getBaseColor() != null)
            pointer = SerializationWriter.writeBytes(pointer, data, (short) bMeta.getBaseColor().ordinal());
        else
            pointer = SerializationWriter.writeBytes(pointer, data, (short) -1);
        pointer = SerializationWriter.writeBytes(pointer, data, (short) bMeta.getPatterns().size());
        for (Pattern pattern : bMeta.getPatterns()) {
            pointer = SerializationWriter.writeBytes(pointer, data, (short) pattern.getColor().ordinal());
            pointer = SerializationWriter.writeBytes(pointer, data, (short) pattern.getPattern().ordinal());
        }
        return data;
    }

    private static final byte[] serializeBookMeta(BookMeta bMeta) {
        int size = 2;
        if (bMeta.hasAuthor())
            size += bMeta.getAuthor().length();
        size += 2;
        if (bMeta.hasTitle())
            size += bMeta.getTitle().length();
        size += 2;
        if (bMeta.hasPages())
            size += 2;
        for (String page : bMeta.getPages())
            size += page.length() + 2;

        byte[] data = new byte[size];
        int pointer = SerializationWriter.writeBytes(0, data, BOOKMETA);
        if (bMeta.hasAuthor())
            pointer = SerializationWriter.writeBytes(pointer, data, bMeta.getAuthor());
        else
            pointer = SerializationWriter.writeBytes(pointer, data, "");
        if (bMeta.hasTitle())
            pointer = SerializationWriter.writeBytes(pointer, data, bMeta.getTitle());
        else
            pointer = SerializationWriter.writeBytes(pointer, data, "");
        if (bMeta.hasPages()) {
            pointer = SerializationWriter.writeBytes(pointer, data, (short) bMeta.getPageCount());
            for (String page : bMeta.getPages())
                pointer = SerializationWriter.writeBytes(pointer, data, page);
        } else
            pointer = SerializationWriter.writeBytes(pointer, data, (short) 0);
        return data;
    }

    @SuppressWarnings("deprecation")
    private static byte[] serializeEnchantmentStorageMeta(EnchantmentStorageMeta meta) {
        byte[] data = new byte[4 + (meta.getStoredEnchants().size() * 4)];
        int pointer = SerializationWriter.writeBytes(0, data, ENCHANTMENTSTORAGEMETA);
        pointer = SerializationWriter.writeBytes(pointer, data, (short) meta.getStoredEnchants().size());
        for (Enchantment ench : meta.getStoredEnchants().keySet()) {
            pointer = SerializationWriter.writeBytes(pointer, data, (short) ench.getId());
            pointer = SerializationWriter.writeBytes(pointer, data, (short) meta.getStoredEnchantLevel(ench));
        }
        meta.getStoredEnchants();
        return data;
    }

    private static byte[] serializeFireworkMeta(FireworkMeta meta) {
        // TODO
        byte[] data = new byte[1024];
        int pointer = SerializationWriter.writeBytes(0, data, FIREWORKMETA);
        pointer = SerializationWriter.writeBytes(pointer, data, (short) meta.getPower());
        pointer = SerializationWriter.writeBytes(pointer, data, (short) meta.getEffectsSize());
        for(FireworkEffect effect :  meta.getEffects()){
            effect.getType();
            effect.hasTrail();
            effect.hasFlicker();
            effect.getColors();
            effect.getColors();
            
            //pointer = SerializationWriter.writeBytes(pointer, data, effect.)
            
        }
        return data;
    }

    private static byte[] serializeLeatherArmorMeta(LeatherArmorMeta meta) {
        // TODO
        meta.getColor();
        return null;
    }

    private static byte[] serializeMapMeta(MapMeta meta) {
        // TODO
        meta.isScaling();
        return null;
    }

    private static byte[] serializePotionMeta(PotionMeta meta) {
        // TODO
        meta.getCustomEffects();
        return null;
    }

    private static byte[] serializeSkullMeta(SkullMeta meta) {
        // TODO
        meta.getOwner();
        return null;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack deserialize(byte[] src) {
        // TODO do this more elegant
        // itemstack
        ItemStack item = null;
        // standart values
        Material material = Material.AIR;
        int amount = 0;
        short durability = 0;
        byte data = 0;
        // metadata values
        String name = "";
        List<String> lore = new ArrayList<String>();
        Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
        List<ItemFlag> flags = new ArrayList<ItemFlag>();
        // bannermeta
        DyeColor baseColor = null;
        List<Pattern> patterns = new ArrayList<Pattern>();
        // bookmeta
        String author = "";
        String title = "";
        List<String> pages = new ArrayList<String>();
        // storedenchantsmeta
        Map<Enchantment, Integer> storedEnchants = new HashMap<Enchantment, Integer>();

        System.out.println(new String(src));
        for (int i = 0; i < src.length - 1; i++) {
            byte[] d = SerializationReader.readBytes(i, src, 2);
            String id = new String(d);
            if (id.equals(new String(MATERIAL)))
                material = deserializeMaterial(i, src);
            else if (id.equals(new String(AMOUNT)))
                amount = deserializeAmount(i, src);
            else if (id.equals(new String(DURABILITY)))
                durability = deserializeDurability(i, src);
            else if (id.equals(new String(MATERIALDATA)))
                data = deserializeMaterialData(i, src);
            else if (id.equals(new String(ENCHANTS)))
                enchants = deserializeEnchantments(i, src);
            else if (id.equals(new String(DISPLAYNAME)))
                name = deserizlizeDisplayName(i, src);
            else if (id.equals(new String(LORE)))
                lore = deserializeLore(i, src);
            else if (id.equals(new String(ITEMFLAGS)))
                flags = deserializeItemFlags(i, src);
            else if (id.equals(new String(BANNERMETA))) {
                baseColor = deserializeBaseColor(i, src);
                patterns = deserializePatterns(i + 2, src);
            } else if (id.equals(new String(BOOKMETA))) {
                author = deserializeAuthor(i, src);
                title = deserializeTitle(i + (author.length() + 4), src);
                pages = deserializePages(i + (title.length() + 4) + (author.length() + 2), src);
            } else if (id.equals(new String(ENCHANTMENTSTORAGEMETA))) {
                storedEnchants = deserializeStoredEnchants(i, src);
            } else if (id.equals(new String(FIREWORKMETA))) {
                // TODO
            } else if (id.equals(new String(LEATHERARMORMETA))) {
                // TODO
            } else if (id.equals(new String(MAPMETA))) {
                // TODO
            } else if (id.equals(new String(POTIONMETA))) {
                // TODO
            } else if (id.equals(new String(SKULLMETA))) {
                // TODO
            }

        }
        // TODO clean this mess up
        item = new ItemStack(material);
        item.setAmount(amount);
        item.setDurability(durability);
        item.getData().setData(data);
        item.addEnchantments(enchants);
        ItemMeta meta = item.getItemMeta();
        if (!name.equals(""))
            meta.setDisplayName(name);
        if (lore.size() != 0)
            meta.setLore(lore);
        if (flags.size() != 0)
            for (ItemFlag flag : flags)
                meta.addItemFlags(flag);
        if (meta instanceof BannerMeta) {
            BannerMeta bMeta = (BannerMeta) meta;
            if (baseColor != null)
                bMeta.setBaseColor(baseColor);
            if (patterns.size() != 0)
                bMeta.setPatterns(patterns);
            meta = bMeta;
        }
        if (meta instanceof BookMeta) {
            BookMeta bMeta = (BookMeta) meta;
            if (!author.equals(""))
                bMeta.setAuthor(author);
            if (!title.equals(""))
                bMeta.setTitle(title);
            if (pages.size() != 0)
                bMeta.setPages(pages);
            meta = bMeta;
        }
        if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta eMeta = (EnchantmentStorageMeta) meta;
            if (storedEnchants.size() != 0)
                for (Enchantment ench : storedEnchants.keySet())
                    eMeta.addStoredEnchant(ench, storedEnchants.get(ench).intValue(), true);
            meta = eMeta;
        }

        item.setItemMeta(meta);
        return item;
    }

    @SuppressWarnings("deprecation")
    private static Material deserializeMaterial(int i, byte[] src) {
        i += MATERIAL.length;
        short itemId = SerializationReader.readShort(i, src);
        return Material.getMaterial(itemId);
    }

    private static int deserializeAmount(int i, byte[] src) {
        i += AMOUNT.length;
        return SerializationReader.readShort(i, src);
    }

    private static short deserializeDurability(int i, byte[] src) {
        i += DURABILITY.length;
        return SerializationReader.readShort(i, src);
    }

    private static byte deserializeMaterialData(int i, byte[] src) {
        i += MATERIALDATA.length;
        return Byte.valueOf((byte) SerializationReader.readByte(i, src));
    }

    @SuppressWarnings("deprecation")
    private static Map<Enchantment, Integer> deserializeEnchantments(int i, byte[] src) {
        Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
        i += ENCHANTS.length;
        short length = SerializationReader.readShort(i, src);
        i += Short.BYTES;
        for (int x = 0; x < length; x++) {
            short enchantId = SerializationReader.readShort(i, src);
            i += Short.BYTES;
            short enchantLvl = SerializationReader.readShort(i, src);
            i += Short.BYTES;
            enchants.put(Enchantment.getById(enchantId), new Integer(enchantLvl));
        }
        return enchants;
    }

    private static String deserizlizeDisplayName(int i, byte[] src) {
        i += DISPLAYNAME.length;
        return SerializationReader.readString(i, src);
    }

    private static List<String> deserializeLore(int i, byte[] src) {
        List<String> lore = new ArrayList<String>();
        i += LORE.length;
        short length = SerializationReader.readShort(i, src);
        i += Short.BYTES;
        for (int x = 0; x < length; x++) {
            String line = SerializationReader.readString(i, src);
            lore.add(line);
            i += line.length();
        }
        return lore;
    }

    private static List<ItemFlag> deserializeItemFlags(int i, byte[] src) {
        List<ItemFlag> flags = new ArrayList<ItemFlag>();
        i += ITEMFLAGS.length;
        short length = SerializationReader.readShort(i, src);
        i += Short.BYTES;
        for (int x = 0; x < length; x++) {
            short flagId = SerializationReader.readShort(i, src);
            flags.add(ItemFlag.values()[flagId]);
        }
        return flags;
    }

    private static DyeColor deserializeBaseColor(int i, byte[] src) {
        DyeColor baseColor = null;
        i += BANNERMETA.length;
        short baseID = SerializationReader.readShort(i, src);
        if (baseID != -1)
            baseColor = DyeColor.values()[baseID];
        return baseColor;
    }

    private static List<Pattern> deserializePatterns(int i, byte[] src) {
        List<Pattern> patterns = new ArrayList<Pattern>();
        i += Short.BYTES;
        short length = SerializationReader.readShort(i, src);
        i += Short.BYTES;
        for (int x = 0; x < length; x++) {
            short color = SerializationReader.readShort(i, src);
            i += Short.BYTES;
            short pattern = SerializationReader.readShort(i, src);
            i += Short.BYTES;
            patterns.add(new Pattern(DyeColor.values()[color], PatternType.values()[pattern]));
        }
        return patterns;
    }

    private static String deserializeAuthor(int i, byte[] src) {
        i += BOOKMETA.length;
        return SerializationReader.readString(i, src);
    }

    private static String deserializeTitle(int i, byte[] src) {
        return SerializationReader.readString(i, src);
    }

    private static List<String> deserializePages(int i, byte[] src) {
        List<String> pages = new ArrayList<String>();
        short length = SerializationReader.readShort(i, src);
        i += 2;
        for (int x = 0; x < length; x++) {
            String current = SerializationReader.readString(i, src);
            pages.add(current);
            i += current.length() + 2;
        }
        return pages;
    }

    @SuppressWarnings("deprecation")
    private static Map<Enchantment, Integer> deserializeStoredEnchants(int i, byte[] src) {
        Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
        i += 2;
        short length = SerializationReader.readShort(i, src);
        i += 2;
        for (int x = 0; x < length; x++) {
            short enchantID = SerializationReader.readShort(i, src);
            short level = SerializationReader.readShort(i + 2, src);
            enchants.put(Enchantment.getById(enchantID), (int) level);
            i += 2;
        }
        return enchants;
    }

}