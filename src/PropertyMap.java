/**
 * Created by Stas on 27.11.2016.
 */
public class PropertyMap {
    private boolean ur,uw,ux,gr,gw,gx,or,ow,ox;
    private boolean hidden,system,readonly;

    public PropertyMap(boolean ur, boolean uw, boolean ux, boolean gr, boolean gw, boolean gx,
                       boolean or, boolean ow, boolean ox, boolean hidden, boolean system, boolean readonly) {
        this.ur = ur;
        this.uw = uw;
        this.ux = ux;
        this.gr = gr;
        this.gw = gw;
        this.gx = gx;
        this.or = or;
        this.ow = ow;
        this.ox = ox;
        this.hidden = hidden;
        this.system = system;
        this.readonly = readonly;
    }

    public boolean isUr() {
        return ur;
    }

    public boolean isUw() {
        return uw;
    }

    public boolean isUx() {
        return ux;
    }

    public boolean isGr() {
        return gr;
    }

    public boolean isGw() {
        return gw;
    }

    public boolean isGx() {
        return gx;
    }

    public boolean isOr() {
        return or;
    }

    public boolean isOw() {
        return ow;
    }

    public boolean isOx() {
        return ox;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isSystem() {
        return system;
    }

    public boolean isReadonly() {
        return readonly;
    }
}
