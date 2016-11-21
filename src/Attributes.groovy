

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class Attributes {
    public boolean hidden
    public boolean system
    public boolean readOnly
    public boolean dir

    public Attributes(byte attributes) {
        dir = (attributes & (1 << 3)) > 0
        hidden = (attributes & (1 << 2)) > 0
        system = (attributes & (1 << 1)) > 0
        readOnly = (attributes & (1 << 0)) > 0
    }

    Attributes(boolean hidden, boolean system, boolean readOnly) {
        this.hidden = hidden
        this.system = system
        this.readOnly = readOnly
    }

    public byte ToByte()
    {
        return (byte)(((dir ? 1 : 0) << 3) |
                ((hidden ? 1 : 0) << 2) |
                ((system ? 1 : 0) << 1) |
                ((readOnly ? 1 : 0) << 0))
    }
}
