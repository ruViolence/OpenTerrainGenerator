package com.pg85.otg.bukkit.util;

import java.lang.reflect.*;
import java.util.*;
import sun.misc.*;
import sun.misc.Unsafe;

public class EnumHelper<E extends Enum<E>> {
	private static class ReflectionHelper {
		public static Class<?>[] getAnonymousClasses (Class<?> clazz) {
			Collection<Class<?>> classes = new ArrayList<> ();
			for (int i = 1;; i++) {
				try {
					classes.add (
						Class.forName (clazz.getName () + "$" + i,
							false, // do not initialize
							Thread.currentThread ().getContextClassLoader ()));
				} catch (ClassNotFoundException e) {
					break;
				}
			}
			for (Class<?> inner : clazz.getDeclaredClasses ()) {
				Collections.addAll (classes, getAnonymousClasses (inner));
			}
			for (Class<?> anon : new ArrayList<> (classes)) {
				Collections.addAll (classes, getAnonymousClasses (anon));
			}
			return classes.toArray (new Class<?>[0]);
		}

		private final static Unsafe UNSAFE;

		static {
			try {
				Field theUnsafe = Unsafe.class.getDeclaredField ("theUnsafe");
				theUnsafe.setAccessible (true);
				UNSAFE = (Unsafe)theUnsafe.get (null);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException (e);
			}
		}

		/**
     * Super dangerous and might have unintended side effects.  Use
     * only for testing, and then with circumspection.
     *
     * @param field
     * @param value
     * @throws ReflectiveOperationException
     */
		public static void setStaticFinalField (Field field, Object value)
			throws ReflectiveOperationException {
			if (!Modifier.isStatic (field.getModifiers ()) || !Modifier.isFinal (field.getModifiers ()))
				throw new IllegalArgumentException ("field should be final static");
			Object fieldBase = UNSAFE.staticFieldBase (field);
			long fieldOffset = UNSAFE.staticFieldOffset (field);
			UNSAFE.putObject (fieldBase, fieldOffset, value);
		}

		public static <E extends Enum<E>> E makeEnum (Class<E> enumType, String name)
			throws ReflectiveOperationException {
			E e             = (E)UNSAFE.allocateInstance (enumType);
			Field nameField = Enum.class.getDeclaredField ("name");
			nameField.setAccessible (true);
			nameField.set (e, name);
			return e;
		}
	}

	private final Class<E> clazz;
	private final Collection<Field> switchFields;
	private final Deque<Memento> undoStack = new ArrayDeque<> ();

	/**
   * Construct an EnumBuster for the given enum class and keep
   * the switch statements of the classes specified in
   * switchUsers in sync with the enum values.
   */
	public EnumHelper (Class<E> clazz, Class<?>... switchUsers) {
		this.clazz   = clazz;
		switchFields = findRelatedSwitchFields (switchUsers);
	}

	/**
   * Make a new enum instance, without adding it to the values
   * array and using the default ordinal of 0.
   */
	public E make (String value) throws ReflectiveOperationException {
		return ReflectionHelper.makeEnum (clazz, value);
	}

	/**
   * This method adds the given enum into the array
   * inside the enum class.  If the enum already
   * contains that particular value, then the value
   * is overwritten with our enum.  Otherwise it is
   * added at the end of the array.
   * <p>
   * In addition, if there is a constant field in the
   * enum class pointing to an enum with our value,
   * then we replace that with our enum instance.
   * <p>
   * The ordinal is either set to the existing position
   * or to the last value.
   * <p>
   * Warning: This should probably never be called,
   * since it can cause permanent changes to the enum
   * values.  Use only in extreme conditions.
   *
   * @param e the enum to add
   */
	public void addByValue (E e) throws ReflectiveOperationException {
		undoStack.push (new Memento ());
		Field valuesField = findValuesField ();

		// we get the current Enum[]
		E[] values = values ();
		for (int i = 0; i < values.length; i++) {
			E value = values[i];
			if (value.name ().equals (e.name ())) {
				setOrdinal (e, value.ordinal ());
				values[i] = e;
				replaceConstant (e);
				return;
			}
		}

		// we did not find it in the existing array, thus
		// append it to the array
		E[] newValues                   = Arrays.copyOf (values, values.length + 1);
		newValues[newValues.length - 1] = e;
		ReflectionHelper.setStaticFinalField (valuesField, newValues);

		int ordinal = newValues.length - 1;
		setOrdinal (e, ordinal);
		addSwitchCase ();
	}

	/**
   * We delete the enum from the values array and set the
   * constant pointer to null.
   *
   * @param e the enum to delete from the type.
   * @return true if the enum was found and deleted;
   * false otherwise
   */
	public boolean deleteByValue (E e) throws ReflectiveOperationException {
		if (e == null)
			throw new NullPointerException ();
		undoStack.push (new Memento ());
		// we get the current E[]
		E[] values = values ();
		for (int i = 0; i < values.length; i++) {
			E value = values[i];
			if (value.name ().equals (e.name ())) {
				E[] newValues = Arrays.copyOf (values, values.length - 1);
				System.arraycopy (values, i + 1, newValues, i, values.length - i - 1);
				for (int j = i; j < newValues.length; j++) {
					setOrdinal (newValues[j], j);
				}
				Field valuesField = findValuesField ();
				ReflectionHelper.setStaticFinalField (valuesField, newValues);
				removeSwitchCase (i);
				blankOutConstant (e);
				return true;
			}
		}
		return false;
	}

	/**
   * Undo the state right back to the beginning when the
   * EnumBuster was created.
   */
	public void restore () throws ReflectiveOperationException {
		while (undo ()) {
			//
		}
	}

	/**
   * Undo the previous operation.
   */
	public boolean undo () throws ReflectiveOperationException {
		Memento memento = undoStack.poll ();
		if (memento == null)
			return false;
		memento.undo ();
		return true;
	}

	/**
   * The only time we ever add a new enum is at the end.
   * Thus all we need to do is expand the switch map arrays
   * by one empty slot.
   */
	private void addSwitchCase () throws ReflectiveOperationException {
		for (Field switchField : switchFields) {
			int[] switches = (int[])switchField.get (null);
			switches       = Arrays.copyOf (switches, switches.length + 1);
			ReflectionHelper.setStaticFinalField (switchField, switches);
		}
	}

	private void replaceConstant (E e) throws ReflectiveOperationException {
		Field[] fields = clazz.getDeclaredFields ();
		for (Field field : fields) {
			if (field.getName ().equals (e.name ())) {
				ReflectionHelper.setStaticFinalField (field, e);
			}
		}
	}

	private void blankOutConstant (E e) throws ReflectiveOperationException {
		Field[] fields = clazz.getDeclaredFields ();
		for (Field field : fields) {
			if (field.getName ().equals (e.name ())) {
				ReflectionHelper.setStaticFinalField (field, null);
			}
		}
	}

	private void setOrdinal (E e, int ordinal)
		throws NoSuchFieldException, IllegalAccessException {
		Field ordinalField = Enum.class.getDeclaredField ("ordinal");
		ordinalField.setAccessible (true);
		ordinalField.set (e, ordinal);
	}

	/**
   * Method to find the values field, set it to be accessible,
   * and return it.
   *
   * @return the values array field for the enum.
   * @throws NoSuchFieldException if the field could not be found
   */
	private Field findValuesField () throws NoSuchFieldException {
		// first we find the static final array that holds
		// the values in the enum class
		Field valuesField = clazz.getDeclaredField ("$VALUES");
		// we mark it to be public
		valuesField.setAccessible (true);
		return valuesField;
	}

	private Collection<Field> findRelatedSwitchFields (Class<?>[] switchUsers) {
		Collection<Field> result = new ArrayList<> ();
		for (Class<?> switchUser : switchUsers) {
			Class<?>[] clazzes = ReflectionHelper.getAnonymousClasses (switchUser);
			for (Class<?> suspect : clazzes) {
				Field[] fields = suspect.getDeclaredFields ();
				for (Field field : fields) {
					if (field.getName ().startsWith ("$SwitchMap$" + clazz.getName ().replace (".", "$"))) {
						field.setAccessible (true);
						result.add (field);
					}
				}
			}
		}
		return result;
	}

	private void removeSwitchCase (int ordinal)
		throws ReflectiveOperationException {
		for (Field switchField : switchFields) {
			int[] switches    = (int[])switchField.get (null);
			int[] newSwitches = Arrays.copyOf (switches, switches.length - 1);
			System.arraycopy (switches, ordinal + 1, newSwitches, ordinal,
				switches.length - ordinal - 1);
			ReflectionHelper.setStaticFinalField (switchField, newSwitches);
		}
	}

	private E[] values () throws ReflectiveOperationException {
		return (E[])findValuesField ().get (null);
	}

	private class Memento {
		private final E[] values;
		private final Map<Field, int[]> savedSwitchFieldValues = new HashMap<> ();

		private Memento () throws ReflectiveOperationException {
			values = values ().clone ();
			for (Field switchField : switchFields) {
				int[] switchArray = (int[])switchField.get (null);
				savedSwitchFieldValues.put (switchField, switchArray.clone ());
			}
		}

		private void undo () throws ReflectiveOperationException {
			Field valuesField = findValuesField ();
			ReflectionHelper.setStaticFinalField (valuesField, values);

			for (int i = 0; i < values.length; i++) {
				setOrdinal (values[i], i);
			}

			// reset all of the constants defined inside the enum
			Map<String, E> valuesMap = new HashMap<> ();
			for (E e : values) {
				valuesMap.put (e.name (), e);
			}
			Field[] constantEnumFields = clazz.getDeclaredFields ();
			for (Field constantEnumField : constantEnumFields) {
				E en = valuesMap.get (constantEnumField.getName ());
				if (en != null) {
					ReflectionHelper.setStaticFinalField (constantEnumField, en);
				}
			}

			for (Map.Entry<Field, int[]> entry : savedSwitchFieldValues.entrySet ()) {
				Field field    = entry.getKey ();
				int[] mappings = entry.getValue ();
				ReflectionHelper.setStaticFinalField (field, mappings);
			}
		}
	}
}
