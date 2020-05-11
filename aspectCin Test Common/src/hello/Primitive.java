package hello;

import java.io.Serializable;

public class Primitive implements Serializable {

	private static final long serialVersionUID = 4720145667877200479L;

	private byte _byte;
	private short _short;
	private int _int;
	private long _long;
	private float _float;
	private double _double;
	private boolean _boolean;
	private char _char;

	public Primitive() {
	}

	public Primitive(byte _byte, short _short, int _int, long _long,
			float _float, double _double, boolean _boolean, char _char) {

		this._byte = _byte;
		this._short = _short;
		this._int = _int;
		this._long = _long;
		this._float = _float;
		this._double = _double;
		this._boolean = _boolean;
		this._char = _char;
	}

	public byte get_byte() {
		return _byte;
	}

	public void set_byte(byte _byte) {
		this._byte = _byte;
	}

	public short get_short() {
		return _short;
	}

	public void set_short(short _short) {
		this._short = _short;
	}

	public int get_int() {
		return _int;
	}

	public void set_int(int _int) {
		this._int = _int;
	}

	public long get_long() {
		return _long;
	}

	public void set_long(long _long) {
		this._long = _long;
	}

	public float get_float() {
		return _float;
	}

	public void set_float(float _float) {
		this._float = _float;
	}

	public double get_double() {
		return _double;
	}

	public void set_double(double _double) {
		this._double = _double;
	}

	public boolean is_boolean() {
		return _boolean;
	}

	public void set_boolean(boolean _boolean) {
		this._boolean = _boolean;
	}

	public char get_char() {
		return _char;
	}

	public void set_char(char _char) {
		this._char = _char;
	}

	@Override
	public String toString() {
		String ret = new String();
		
		ret += "byte = " + this._byte + "\n";
		ret += "short = " + this._short + "\n";
		ret += "int = " + this._int + "\n";
		ret += "long = " + this._long + "\n";
		ret += "float = " + this._float + "\n";
		ret += "double = " + this._double + "\n";
		ret += "boolean = " + this._boolean + "\n";
		ret += "char = " + this._char  + "\n";
		
		return ret;
	}
}
