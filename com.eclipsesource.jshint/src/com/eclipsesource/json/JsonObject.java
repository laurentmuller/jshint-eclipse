/*******************************************************************************
 * Copyright (c) 2008, 2013 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.json;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.eclipsesource.json.JsonObject.Member;

/**
 * Represents a JSON object. A JSON object contains a sequence of members, which
 * are pairs of a name and a JSON value (see {@link JsonValue}). Although JSON
 * objects should be used for unordered collections, this class stores members
 * in document order.
 * <p>
 * Members can be added using one of the <code>add(name, value)</code> methods.
 * Accepted values are either instances of {@link JsonValue}, strings, primitive
 * numbers, or boolean values. To override values in an object, the
 * <code>set(name, value)</code> methods can be used. However, not that the
 * <code>add</code> methods perform better than <code>set</code>.
 * </p>
 * <p>
 * Members can be accessed by their name using {@link #get(String)}. A list of
 * all names can be obtained from the method {@link #names()}. This class also
 * supports iterating over the members in document order using an
 * {@link #iterator()} or an enhanced for loop:
 * </p>
 *
 * <pre>
 * for( Member member : jsonObject ) {
 *   String name = member.getName();
 *   JsonValue value = member.getValue();
 *   ...
 * }
 * </pre>
 * <p>
 * Note that this class is <strong>not thread-safe</strong>. If multiple threads
 * access a <code>JsonObject</code> instance concurrently, while at least one of
 * these threads modifies the contents of this object, access to the instance
 * must be synchronized externally. Failure to do so may lead to an inconsistent
 * state.
 * </p>
 * <p>
 * This class is <strong>not supposed to be extended</strong> by clients.
 * </p>
 */
@SuppressWarnings("serial")
// use default serial UID
public class JsonObject extends JsonValue implements Iterable<Member> {

	/**
	 * Represents a member of a JSON object, i.e. a pair of name and value.
	 */
	public static class Member {

		private final String name;
		private final JsonValue value;

		Member(final String name, final JsonValue value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Member other = (Member) obj;
			return name.equals(other.name) && value.equals(other.value);
		}

		/**
		 * Returns the name of this member.
		 *
		 * @return the name of this member, never <code>null</code>
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the value of this member.
		 *
		 * @return the value of this member, never <code>null</code>
		 */
		public JsonValue getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			int result = 1;
			result = 31 * result + name.hashCode();
			result = 31 * result + value.hashCode();
			return result;
		}

	}

	static class HashIndexTable {

		private final byte[] hashTable = new byte[32]; // must be a power of two

		public HashIndexTable() {
		}

		public HashIndexTable(final HashIndexTable original) {
			System.arraycopy(original.hashTable, 0, hashTable, 0,
					hashTable.length);
		}

		private int hashSlotFor(final Object element) {
			return element.hashCode() & hashTable.length - 1;
		}

		void add(final String name, final int index) {
			final int slot = hashSlotFor(name);
			if (index < 0xff) {
				// increment by 1, 0 stands for empty
				hashTable[slot] = (byte) (index + 1);
			} else {
				hashTable[slot] = 0;
			}
		}

		int get(final Object name) {
			final int slot = hashSlotFor(name);
			// subtract 1, 0 stands for empty
			return (hashTable[slot] & 0xff) - 1;
		}

		void remove(final int index) {
			for (int i = 0; i < hashTable.length; i++) {
				if (hashTable[i] == index + 1) {
					hashTable[i] = 0;
				} else if (hashTable[i] > index + 1) {
					hashTable[i]--;
				}
			}
		}

	}

	/**
	 * Reads a JSON object from the given reader.
	 * <p>
	 * Characters are read in chunks and buffered internally, therefore wrapping
	 * an existing reader in an additional <code>BufferedReader</code> does
	 * <strong>not</strong> improve reading performance.
	 * </p>
	 *
	 * @param reader
	 *            the reader to read the JSON object from
	 * @return the JSON object that has been read
	 * @throws IOException
	 *             if an I/O error occurs in the reader
	 * @throws ParseException
	 *             if the input is not valid JSON
	 * @throws UnsupportedOperationException
	 *             if the input does not contain a JSON object
	 */
	public static JsonObject readFrom(final Reader reader) throws IOException {
		return JsonValue.readFrom(reader).asObject();
	}

	/**
	 * Reads a JSON object from the given string.
	 *
	 * @param string
	 *            the string that contains the JSON object
	 * @return the JSON object that has been read
	 * @throws ParseException
	 *             if the input is not valid JSON
	 * @throws IOException
	 *             if the input does not contain a JSON object
	 */
	public static JsonObject readFrom(final String string) throws IOException {
		return JsonValue.readFrom(string).asObject();
	}

	/**
	 * Returns an unmodifiable JsonObject for the specified one. This method
	 * allows to provide read-only access to a JsonObject.
	 * <p>
	 * The returned JsonObject is backed by the given object and reflect changes
	 * that happen to it. Attempts to modify the returned JsonObject result in
	 * an <code>UnsupportedOperationException</code>.
	 * </p>
	 *
	 * @param object
	 *            the JsonObject for which an unmodifiable JsonObject is to be
	 *            returned
	 * @return an unmodifiable view of the specified JsonObject
	 */
	public static JsonObject unmodifiableObject(final JsonObject object) {
		return new JsonObject(object, true);
	}

	private final List<String> names;

	private final List<JsonValue> values;

	private transient HashIndexTable table;

	/**
	 * Creates a new empty JsonObject.
	 */
	public JsonObject() {
		names = new ArrayList<>();
		values = new ArrayList<>();
		table = new HashIndexTable();
	}

	/**
	 * Creates a new JsonObject, initialized with the contents of the specified
	 * JSON object.
	 *
	 * @param object
	 *            the JSON object to get the initial contents from, must not be
	 *            <code>null</code>
	 */
	public JsonObject(final JsonObject object) {
		this(object, false);
	}

	private JsonObject(final JsonObject object, final boolean unmodifiable) {
		Objects.requireNonNull(object, "The 'object' parameter is null.");
		if (unmodifiable) {
			names = Collections.unmodifiableList(object.names);
			values = Collections.unmodifiableList(object.values);
		} else {
			names = new ArrayList<>(object.names);
			values = new ArrayList<>(object.values);
		}
		table = new HashIndexTable();
		updateHashIndex();
	}

	/**
	 * Adds a new member at the end of this object, with the specified name and
	 * the JSON representation of the specified <code>boolean</code> value.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Adding a
	 * member with a name that already exists in the object will add another
	 * member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong>
	 * <em>add</em> is much faster than <em>set</em></strong> (because it does
	 * not need to search for existing members). Therefore <em>add</em> should
	 * be preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject add(final String name, final boolean value) {
		add(name, valueOf(value));
		return this;
	}

	/**
	 * Adds a new member at the end of this object, with the specified name and
	 * the JSON representation of the specified <code>double</code> value.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Adding a
	 * member with a name that already exists in the object will add another
	 * member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong>
	 * <em>add</em> is much faster than <em>set</em></strong> (because it does
	 * not need to search for existing members). Therefore <em>add</em> should
	 * be preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject add(final String name, final double value) {
		add(name, valueOf(value));
		return this;
	}

	/**
	 * Adds a new member at the end of this object, with the specified name and
	 * the JSON representation of the specified <code>float</code> value.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Adding a
	 * member with a name that already exists in the object will add another
	 * member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong>
	 * <em>add</em> is much faster than <em>set</em></strong> (because it does
	 * not need to search for existing members). Therefore <em>add</em> should
	 * be preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject add(final String name, final float value) {
		add(name, valueOf(value));
		return this;
	}

	/**
	 * Adds a new member at the end of this object, with the specified name and
	 * the JSON representation of the specified <code>int</code> value.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Adding a
	 * member with a name that already exists in the object will add another
	 * member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong>
	 * <em>add</em> is much faster than <em>set</em></strong> (because it does
	 * not need to search for existing members). Therefore <em>add</em> should
	 * be preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject add(final String name, final int value) {
		add(name, valueOf(value));
		return this;
	}

	/**
	 * Adds a new member at the end of this object, with the specified name and
	 * the specified JSON value.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Adding a
	 * member with a name that already exists in the object will add another
	 * member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong>
	 * <em>add</em> is much faster than <em>set</em></strong> (because it does
	 * not need to search for existing members). Therefore <em>add</em> should
	 * be preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add, must not be <code>null</code>
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject add(final String name, final JsonValue value) {
		Objects.requireNonNull(name, "The 'name' parameter is null.");
		Objects.requireNonNull(value, "The 'value' parameter is null.");
		table.add(name, names.size());
		names.add(name);
		values.add(value);
		return this;
	}

	/**
	 * Adds a new member at the end of this object, with the specified name and
	 * the JSON representation of the specified <code>long</code> value.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Adding a
	 * member with a name that already exists in the object will add another
	 * member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong>
	 * <em>add</em> is much faster than <em>set</em></strong> (because it does
	 * not need to search for existing members). Therefore <em>add</em> should
	 * be preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject add(final String name, final long value) {
		add(name, valueOf(value));
		return this;
	}

	/**
	 * Adds a new member at the end of this object, with the specified name and
	 * the JSON representation of the specified string.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Adding a
	 * member with a name that already exists in the object will add another
	 * member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong>
	 * <em>add</em> is much faster than <em>set</em></strong> (because it does
	 * not need to search for existing members). Therefore <em>add</em> should
	 * be preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject add(final String name, final String value) {
		add(name, valueOf(value));
		return this;
	}

	@Override
	public JsonObject asObject() {
		return this;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final JsonObject other = (JsonObject) obj;
		return names.equals(other.names) && values.equals(other.values);
	}

	/**
	 * Returns the value of the member with the specified name in this object.
	 * If this object contains multiple members with the given name, this method
	 * will return the last one.
	 *
	 * @param name
	 *            the name of the member whose value is to be returned
	 * @return the value of the last member with the specified name, or
	 *         <code>null</code> if this object does not contain a member with
	 *         that name
	 */
	public JsonValue get(final String name) {
		Objects.requireNonNull(name, "The 'name' parameter is null.");
		final int index = indexOf(name);
		return index != -1 ? values.get(index) : null;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + names.hashCode();
		result = 31 * result + values.hashCode();
		return result;
	}

	/**
	 * Returns <code>true</code> if this object contains no members.
	 *
	 * @return <code>true</code> if this object contains no members
	 */
	public boolean isEmpty() {
		return names.isEmpty();
	}

	@Override
	public boolean isObject() {
		return true;
	}

	/**
	 * Returns an iterator over the members of this object in document order.
	 * The returned iterator cannot be used to modify this object.
	 *
	 * @return an iterator over the members of this object
	 */
	@Override
	public Iterator<Member> iterator() {
		final Iterator<String> namesIterator = names.iterator();
		final Iterator<JsonValue> valuesIterator = values.iterator();
		return new Iterator<JsonObject.Member>() {

			@Override
			public boolean hasNext() {
				return namesIterator.hasNext();
			}

			@Override
			public Member next() {
				final String name = namesIterator.next();
				final JsonValue value = valuesIterator.next();
				return new Member(name, value);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	/**
	 * Returns a list of the names in this object in document order. The
	 * returned list is backed by this object and will reflect subsequent
	 * changes. It cannot be used to modify this object. Attempts to modify the
	 * returned list will result in an exception.
	 *
	 * @returns a list of the names in this object
	 */
	public List<String> names() {
		return Collections.unmodifiableList(names);
	}

	/**
	 * Removes a member with the specified name from this object. If this object
	 * contains multiple members with the given name, only the last one is
	 * removed. If this object does not contain a member with the specified
	 * name, the object is not modified.
	 *
	 * @param name
	 *            the name of the member to remove
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject remove(final String name) {
		Objects.requireNonNull(name, "The 'name' parameter is null.");
		final int index = indexOf(name);
		if (index != -1) {
			table.remove(index);
			names.remove(index);
			values.remove(index);
		}
		return this;
	}

	/**
	 * Sets the value of the member with the specified name to the JSON
	 * representation of the specified <code>boolean</code> value. If this
	 * object does not contain a member with this name, a new member is added at
	 * the end of the object. If this object contains multiple members with this
	 * name, only the last one is changed.
	 * <p>
	 * This method should <strong>only be used to modify existing
	 * objects</strong>. To fill a new object with members, the method
	 * <code>add(name, value)</code> should be preferred which is much faster
	 * (as it does not need to search for existing members).
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject set(final String name, final boolean value) {
		set(name, valueOf(value));
		return this;
	}

	/**
	 * Sets the value of the member with the specified name to the JSON
	 * representation of the specified <code>double</code> value. If this object
	 * does not contain a member with this name, a new member is added at the
	 * end of the object. If this object contains multiple members with this
	 * name, only the last one is changed.
	 * <p>
	 * This method should <strong>only be used to modify existing
	 * objects</strong>. To fill a new object with members, the method
	 * <code>add(name, value)</code> should be preferred which is much faster
	 * (as it does not need to search for existing members).
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject set(final String name, final double value) {
		set(name, valueOf(value));
		return this;
	}

	/**
	 * Sets the value of the member with the specified name to the JSON
	 * representation of the specified <code>float</code> value. If this object
	 * does not contain a member with this name, a new member is added at the
	 * end of the object. If this object contains multiple members with this
	 * name, only the last one is changed.
	 * <p>
	 * This method should <strong>only be used to modify existing
	 * objects</strong>. To fill a new object with members, the method
	 * <code>add(name, value)</code> should be preferred which is much faster
	 * (as it does not need to search for existing members).
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject set(final String name, final float value) {
		set(name, valueOf(value));
		return this;
	}

	/**
	 * Sets the value of the member with the specified name to the JSON
	 * representation of the specified <code>int</code> value. If this object
	 * does not contain a member with this name, a new member is added at the
	 * end of the object. If this object contains multiple members with this
	 * name, only the last one is changed.
	 * <p>
	 * This method should <strong>only be used to modify existing
	 * objects</strong>. To fill a new object with members, the method
	 * <code>add(name, value)</code> should be preferred which is much faster
	 * (as it does not need to search for existing members).
	 * </p>
	 *
	 * @param name
	 *            the name of the member to replace
	 * @param value
	 *            the value to set to the member
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject set(final String name, final int value) {
		set(name, valueOf(value));
		return this;
	}

	/**
	 * Sets the value of the member with the specified name to the specified
	 * JSON value. If this object does not contain a member with this name, a
	 * new member is added at the end of the object. If this object contains
	 * multiple members with this name, only the last one is changed.
	 * <p>
	 * This method should <strong>only be used to modify existing
	 * objects</strong>. To fill a new object with members, the method
	 * <code>add(name, value)</code> should be preferred which is much faster
	 * (as it does not need to search for existing members).
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add, must not be <code>null</code>
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject set(final String name, final JsonValue value) {
		Objects.requireNonNull(name, "The 'name' parameter is null.");
		Objects.requireNonNull(value, "The 'value' parameter is null.");
		final int index = indexOf(name);
		if (index != -1) {
			values.set(index, value);
		} else {
			table.add(name, names.size());
			names.add(name);
			values.add(value);
		}
		return this;
	}

	/**
	 * Sets the value of the member with the specified name to the JSON
	 * representation of the specified <code>long</code> value. If this object
	 * does not contain a member with this name, a new member is added at the
	 * end of the object. If this object contains multiple members with this
	 * name, only the last one is changed.
	 * <p>
	 * This method should <strong>only be used to modify existing
	 * objects</strong>. To fill a new object with members, the method
	 * <code>add(name, value)</code> should be preferred which is much faster
	 * (as it does not need to search for existing members).
	 * </p>
	 *
	 * @param name
	 *            the name of the member to replace
	 * @param value
	 *            the value to set to the member
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject set(final String name, final long value) {
		set(name, valueOf(value));
		return this;
	}

	/**
	 * Sets the value of the member with the specified name to the JSON
	 * representation of the specified string. If this object does not contain a
	 * member with this name, a new member is added at the end of the object. If
	 * this object contains multiple members with this name, only the last one
	 * is changed.
	 * <p>
	 * This method should <strong>only be used to modify existing
	 * objects</strong>. To fill a new object with members, the method
	 * <code>add(name, value)</code> should be preferred which is much faster
	 * (as it does not need to search for existing members).
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	public JsonObject set(final String name, final String value) {
		set(name, valueOf(value));
		return this;
	}

	/**
	 * Returns the number of members (i.e. name/value pairs) in this object.
	 *
	 * @return the number of members in this object
	 */
	public int size() {
		return names.size();
	}

	@Override
	protected void write(final JsonWriter writer) throws IOException {
		writer.writeObject(this);
	}

	private synchronized void readObject(final ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		table = new HashIndexTable();
		updateHashIndex();
	}

	private void updateHashIndex() {
		final int size = names.size();
		for (int i = 0; i < size; i++) {
			table.add(names.get(i), i);
		}
	}

	int indexOf(final String name) {
		final int index = table.get(name);
		if (index != -1 && name.equals(names.get(index))) {
			return index;
		}
		return names.lastIndexOf(name);
	}

}
