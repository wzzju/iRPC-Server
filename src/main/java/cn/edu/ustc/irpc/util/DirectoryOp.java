package cn.edu.ustc.irpc.util;

import java.io.File;

public class DirectoryOp {

    boolean flag = false;

    public boolean deleteFile(String filePath) {// 删除单个文件
        boolean flag = false;
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {// 路径为文件且不为空则进行删除
            file.delete();// 文件删除
            flag = true;
        }
        return flag;
    }

    public boolean deleteDir(String dirPath) {// 删除目录（文件夹）以及目录下的文件
        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!dirPath.endsWith(File.separator)) {
            dirPath = dirPath + File.separator;
        }
        File dirFile = new File(dirPath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();// 获得传入路径下的所有文件
        for (int i = 0; i < files.length; i++) {// 循环遍历删除文件夹下的所有文件(包括子目录)
            if (files[i].isFile()) {// 删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (flag) {
                    System.out.println(files[i].getAbsolutePath() + " is deleted successfully!");
                } else {
                    System.err.println("Fail to delete " + files[i].getAbsolutePath());
                    break;// 如果删除失败，则跳出
                }
            } else {// 运用递归，删除子目录
                flag = deleteDir(files[i].getAbsolutePath());
                if (!flag) {
                    System.err.println("Fail to delete " + files[i].getAbsolutePath());
                    break;// 如果删除失败，则跳出
                }
            }
        }
        if (!flag)
            return false;
        if (dirFile.delete()) {// 删除当前目录
            return true;
        } else {
            System.err.println("Fail to delete " + dirFile.getAbsolutePath());
            return false;
        }
    }

    public boolean createDir(String dirPath) {
        if (!dirPath.endsWith(File.separator)) {
            dirPath = dirPath + File.separator;
        }
        File dir = new File(dirPath);
        if (dir.exists()) {//如果存在所要创建的目录则删除该目录
            deleteDir(dirPath);
        }
        if (dir.mkdir()) {
            System.out.println("Create directory " + dirPath + " successfully!");
            return true;
        } else {
            System.err.println("Fail to create directory " + dirPath);
            return false;
        }
    }
}
