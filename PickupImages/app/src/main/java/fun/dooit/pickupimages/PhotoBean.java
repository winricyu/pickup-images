package fun.dooit.pickupimages;

/**
 * Created by Eric on 2017/8/15.
 */

public class PhotoBean {

    private String _ID;
    private String path;
    private String size;
    private String fileName;
    private String mimeType;
    private String title;
    private boolean isChecked;

    public PhotoBean() {
        init();
    }

    private void init() {
        _ID = "";
        path = "";
        size = "";
        fileName = "";
        mimeType = "";
        title = "";
        isChecked = false;
    }

    public String get_ID() {
        return _ID;
    }

    public void set_ID(String _ID) {
        this._ID = _ID;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public String toString() {
        return "[" + _ID + " " + path + " " + isChecked + "]";
    }
}
