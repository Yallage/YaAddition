package io.github.apjifengc.yaaddition.core;

import io.github.apjifengc.yaaddition.YaAddition;
import io.github.apjifengc.yaaddition.addition.AdditionBlock;
import io.github.apjifengc.yaaddition.addition.AdditionMaterial;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class SpecialNoteBlock implements Listener {
    private static final Map<Instrument, Sound> soundMap = new HashMap<>();
    static {
        soundMap.put(Instrument.PIANO, Sound.BLOCK_NOTE_BLOCK_HARP);
        soundMap.put(Instrument.BASS_DRUM, Sound.BLOCK_NOTE_BLOCK_BASEDRUM);
        soundMap.put(Instrument.SNARE_DRUM, Sound.BLOCK_NOTE_BLOCK_SNARE);
        soundMap.put(Instrument.STICKS, Sound.BLOCK_NOTE_BLOCK_HAT);
        soundMap.put(Instrument.BASS_GUITAR, Sound.BLOCK_NOTE_BLOCK_BASS);
        soundMap.put(Instrument.FLUTE, Sound.BLOCK_NOTE_BLOCK_FLUTE);
        soundMap.put(Instrument.BELL, Sound.BLOCK_NOTE_BLOCK_BELL);
        soundMap.put(Instrument.GUITAR, Sound.BLOCK_NOTE_BLOCK_GUITAR);
        soundMap.put(Instrument.CHIME, Sound.BLOCK_NOTE_BLOCK_CHIME);
        soundMap.put(Instrument.XYLOPHONE, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE);
        soundMap.put(Instrument.IRON_XYLOPHONE, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE);
        soundMap.put(Instrument.COW_BELL, Sound.BLOCK_NOTE_BLOCK_COW_BELL);
        soundMap.put(Instrument.DIDGERIDOO, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO);
        soundMap.put(Instrument.BIT, Sound.BLOCK_NOTE_BLOCK_BIT);
        soundMap.put(Instrument.BANJO, Sound.BLOCK_NOTE_BLOCK_BANJO);
        soundMap.put(Instrument.PLING, Sound.BLOCK_NOTE_BLOCK_PLING);
    }

    public SpecialNoteBlock(YaAddition plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    void onInteract(PlayerInteractEvent event) {
        if (event.hasBlock() && event.getClickedBlock().getType() == Material.NOTE_BLOCK) {
            if (((NoteBlock) event.getClickedBlock().getBlockData()).getNote().getId() != 0) return;
            switch (event.getAction()) {
                case RIGHT_CLICK_BLOCK:
                    cycle(event.getClickedBlock());
                    playNote(event.getClickedBlock());
                    event.getPlayer().incrementStatistic(Statistic.NOTEBLOCK_TUNED);
                    event.setCancelled(true);
                    break;
                case LEFT_CLICK_BLOCK:
                    event.getPlayer().incrementStatistic(Statistic.NOTEBLOCK_PLAYED);
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    void onBreak(BlockBreakEvent event) {
        BlockStorage.clear(event.getBlock().getLocation());
    }

    @EventHandler
    void onNote(NotePlayEvent event) {
        if (((NoteBlock) event.getBlock().getBlockData()).getNote().getId() == 0) playNote(event.getBlock());
        event.setCancelled(true);
    }

    @EventHandler
    void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.getBlock().getType() == Material.NOTE_BLOCK && AdditionBlock.isAddition(event.getBlock())) {
            AdditionBlock.asAdditionCopy(event.getBlock()).getMaterial().getState().setData(event.getBlock());
            event.setCancelled(true);
        }
    }

    static public int getNote(Location location) {
        Object value = BlockStorage.get(location, "note");
        if (value instanceof Integer) {
            return (Integer) value;
        } else {
            return 0;
        }
    }

    static public int getNote(Block block) {
        return getNote(block.getLocation());
    }

    static public void setNote(Block block, int note) {
        BlockStorage.set(block.getLocation(), "note", note);
    }

    static public void cycle(Block block) {
        setNote(block, (getNote(block)==24?0:1+getNote(block)));
    }

    public void playNote(Block block) {
        if (block.getRelative(BlockFace.UP).isEmpty()) {
            Location location = block.getLocation();
            World world = block.getWorld();
            NoteBlock noteBlock = (NoteBlock) block.getBlockData();
            int note = getNote(block);
            float pitch = (float)Math.pow(2.0D, (note - 12) / 12.0D);
            world.playSound(location, soundMap.get(noteBlock.getInstrument()), SoundCategory.RECORDS, 3.0F, pitch);
            world.spawnParticle(Particle.NOTE, location.add(0.5D, 1.2D, 0.5D), 0, note / 24.0D, 0.0D, 0.0D);
        }
    }
}
