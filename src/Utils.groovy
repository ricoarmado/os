

import org.apache.commons.io.FilenameUtils
/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class Utils {
    static boolean CheckPath(String path, boolean fullPath = false){
        if(fullPath && path[0] != '/')
            throw new IllegalArgumentException("Путь не является абсолютным")
        if(path.empty)
            throw new IllegalArgumentException("Неверно задан путь")
        if(path == "/")
            true
        if(path[0] == '')
            path = path.substring(1)
        String[] pathList = path.split('/')
        String pathPart
        for(int i = 0; i < pathList.length; i++){
            pathPart = pathList[i]
        }
    }
    static String getFullPath(String path, String currentPath){
        CheckPath(path)
        if(path[0] == '/')
            return path
        CheckPath(currentPath,true)
        StringBuffer buffer = new StringBuffer(currentPath)
        if(currentPath != "/")
            buffer.append('/')
        buffer.append(path)
        buffer.replaceAll("/./", "/")
        String cleaned = buffer.toString()
        int startIndex, endIndex
        String sliced
        while ((endIndex = cleaned.indexOf("/../")) >= 0){
            sliced = cleaned.substring(0,endIndex)
            startIndex = sliced.indexOf('/')
            startIndex++
            buffer.delete(startIndex, endIndex - startIndex + 4)
            cleaned = buffer.toString()
        }
        buffer.toString()
    }
    static String getDirectoryName(String path){
        FilenameUtils.getPath(path)
    }
    static String getFileName(String path){
        FilenameUtils.getName(path)
    }

    static String getFileNameWithoutExtension(String path) {
        String name = FilenameUtils.getName(path)
        String ext = FilenameUtils.getExtension(path)
        name.replace(ext,"")
    }
    static String getExtension(String path){
        FilenameUtils.getExtension(path)
    }

    static RightsGroup getRightsGroup(boolean r, boolean w, boolean x){
        new RightsGroup(r,w,x)
    }
    static RightsGroup getAccess(short currentUserId,short currentGroupId, short fileUserId, short fileGroupId, AccessRights accessRights){
        if(currentUserId == 1 as short  && currentGroupId == 1 as short) {
            return getRightsGroup(true,true,true)
        }
        else if(currentUserId == fileUserId){
            return accessRights.user
        }
        else if(currentGroupId == fileGroupId){
            return accessRights.group
        }
        accessRights.others
    }
    static short bytesToShort(byte[] bytes){
        (short)(bytes[0] << 8 | bytes[1] & 0xFF)
    }
    static byte[] shortToBytes(short value) {
        byte[] returnByteArray = new byte[2];
        returnByteArray[0] = (byte) (value & 0xff);
        returnByteArray[1] = (byte) ((value >>> 8) & 0xff);
        return returnByteArray;
    }
}
