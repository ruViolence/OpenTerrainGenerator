package com.pg85.otg.forge.mixin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import com.pg85.otg.constants.Constants;

/**
 * <p>A companion plugin for a mixin configuration object. Objects implementing
 * this interface get some limited power of veto over the mixin load process as
 * well as an opportunity to apply their own transformations to the target class
 * pre- and post-transform. Since all methods in this class are called
 * indirectly from the transformer, the same precautions as for writing class
 * transformers should be taken. Implementors should take care to not reference
 * any game classes, and avoid referencing other classes in their own mod except
 * those specificially designed to be available at early startup, such as
 * coremod classes or other standalone bootstrap objects.</p>
 * 
 * <p>Instances of plugins are created by specifying the "plugin" key in the
 * mixin config JSON as the fully-qualified class name of a class implementing
 * this interface.</p>
*/
public class MixinConfigPlugin implements IMixinConfigPlugin
{	
    /**
     * Called after the plugin is instantiated, do any setup here.
     * 
     * @param mixinPackage The mixin root package from the config
    */	
	@Override
	public void onLoad(String mixinPackage) { }

    /**
     * Called only if the "referenceMap" key in the config is <b>not</b> set.
     * This allows the refmap file name to be supplied by the plugin
     * programatically if desired. Returning <code>null</code> will revert to
     * the default behaviour of using the default refmap json file.
     * 
     * @return Path to the refmap resource or null to revert to the default
    */	
	@Override
	public String getRefMapperConfig() { return null; }

    /**
     * Called during mixin intialisation, allows this plugin to control whether
     * a specific will be applied to the specified target. Returning false will
     * remove the target from the mixin's target set, and if all targets are
     * removed then the mixin will not be applied at all.
     * 
     * @param targetClassName Fully qualified class name of the target class
     * @param mixinClassName Fully qualified class name of the mixin
     * @return True to allow the mixin to be applied, or false to remove it from
     *      target's mixin set
    */
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return true; }

    /**
     * Called after all configurations are initialised, this allows this plugin
     * to observe classes targetted by other mixin configs and optionally remove
     * targets from its own set. The set myTargets is a direct view of the
     * targets collection in this companion config and keys may be removed from
     * this set to suppress mixins in this config which target the specified
     * class. Adding keys to the set will have no effect.
     * 
     * @param myTargets Target class set from the companion config
     * @param otherTargets Target class set incorporating targets from all other
     *      configs, read-only
    */	
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
	{
		org.apache.logging.log4j.Logger logger = LogManager.getLogger(Constants.MOD_ID_SHORT);
		List<String> cannotBeDuplicateMixins = Arrays.asList("net.minecraft.world.gen.feature.structure.Structure");
		try {
			Field field = myTargets.getClass().getSuperclass().getDeclaredField("c");
			field.setAccessible(true);
			Collection<String> wrappedSet = (Collection<String>)field.get(myTargets);
			myTargets.stream().filter(a -> cannotBeDuplicateMixins.contains(a) && otherTargets.contains(a)).collect(Collectors.toList()).forEach(a -> {
				logger.warn("Detected conflicting mixin for class " + a + " from other mod, disabling OTG mixin to avoid problems.");
				wrappedSet.remove(a);
			});
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

    /**
     * After mixins specified in the configuration have been processed, this
     * method is called to allow the plugin to add any additional mixins to
     * load. It should return a list of mixin class names or return null if the
     * plugin does not wish to append any mixins of its own.
     * 
     * @return additional mixins to apply
    */	
	@Override
	public List<String> getMixins() { return null; }

    /**
     * Called immediately <b>before</b> a mixin is applied to a target class,
     * allows any pre-application transformations to be applied.
     * 
     * @param targetClassName Transformed name of the target class
     * @param targetClass Target class tree
     * @param mixinClassName Name of the mixin class
     * @param mixinInfo Information about this mixin
    */	
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    /**
     * Called immediately <b>after</b> a mixin is applied to a target class,
     * allows any post-application transformations to be applied.
     * 
     * @param targetClassName Transformed name of the target class
     * @param targetClass Target class tree
     * @param mixinClassName Name of the mixin class
     * @param mixinInfo Information about this mixin
    */	
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {	}
}
