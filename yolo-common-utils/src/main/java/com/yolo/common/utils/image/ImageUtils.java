package com.yolo.common.utils.image;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;


/**
 * 图片比例缩放图剪截(自动缩放比例，按照长宽中最先达到指定长宽的尺寸为基准)
 * @author user
 *
 */
public class ImageUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
		InputStream in=new FileInputStream(new File("D:\\zz/test_1.jpg"));
		File fileOut = new File("D:\\zz/cc/dd/test_1_ccccc222_CCC.jpg");
		FileUtils.forceMkdirParent(fileOut);
		OutputStream out=new FileOutputStream(fileOut);
		ImageUtils.saveProcessImage(in, out, 100, 100);
		in.close();
		out.close();
		
	}
	
	
	public static Image processImage(Image image,int baseWidth,int baseHeight)throws Exception{
		BufferedImage imageBuff=null;
		if(baseWidth>0 && baseHeight>0){
			imageBuff=new BufferedImage(baseWidth,baseHeight,BufferedImage.TYPE_3BYTE_BGR);
			double zoomW=1.0*image.getWidth(null)/baseWidth;
			double zoomH=1.0*image.getHeight(null)/baseHeight;
			double pointAX=0;
			double pointAY=0;
			double pointBX=0;
			double pointBY=0;
			if(zoomW<zoomH){
				image=image.getScaledInstance(baseWidth, (int)Math.round(image.getHeight(null)*1.0/zoomW),Image.SCALE_SMOOTH);
				pointAX=0;
				pointAY=1.0*(image.getHeight(null)-baseHeight)/2.0;
				pointBX=image.getWidth(null);
				pointBY=image.getHeight(null) - 1.0*(image.getHeight(null)-baseHeight)/2.0;
			}else{
				image=image.getScaledInstance((int)Math.round(image.getWidth(null)*1.0/zoomH),baseHeight,Image.SCALE_SMOOTH);
				pointAX=1.0*(image.getWidth(null)-baseWidth)/2.0;
				pointAY=0;
				pointBX=image.getWidth(null) - 1.0*(image.getWidth(null)-baseWidth)/2.0;
				pointBY=image.getHeight(null);
			}
			//double 
			imageBuff.getGraphics().drawImage(image, 0, 0, baseWidth, baseHeight,(int)Math.round(pointAX),(int)Math.round(pointAY),(int)Math.round(pointBX),(int)Math.round(pointBY), null);
		}else{
			imageBuff=new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_3BYTE_BGR);
			imageBuff.getGraphics().drawImage(image, 0, 0, null);
		}
		return imageBuff;
	} 
	
	
	
	
	public static void saveProcessImage(String fileSourcePath, String filePath,int baseWidth,int baseHeight)throws Exception{
		File file=new File(filePath);
		File fileSource=new File(fileSourcePath);
		saveProcessImage(file, fileSource,baseWidth,baseHeight);
	}
	
	public static void saveProcessImage(File fileSource,File file,int baseWidth,int baseHeight)throws Exception{
		FileOutputStream out=new FileOutputStream(file);
		saveProcessImage(new FileInputStream(fileSource), out,baseWidth,baseHeight);
		out.flush();
		out.close();
	}
	
	public static void saveProcessImage(InputStream in, OutputStream out,int baseWidth,int baseHeight)throws Exception{
		Image imageSource=ImageIO.read(in);
		saveProcessImage(imageSource, out,baseWidth,baseHeight);
	}
	
	public static void saveProcessImage(InputStream in, List<ImageParam> paramList)throws Exception{
		Image imageSource=ImageIO.read(in);
		if(paramList!=null){
			for (ImageParam imageParam : paramList) {
				String path=imageParam.getPath();
				int width=imageParam.getBaseWidth();
				int height=imageParam.getBaseHeight();
				File file = new File(path);
				FileUtils.forceMkdirParent(file);
				OutputStream out=new FileOutputStream(file);
				saveProcessImage(imageSource, out ,width,height);
				out.close();
			}
		}
	}
	
	
	public static void saveProcessImage(Image imageSource, OutputStream out,int baseWidth,int baseHeight)throws Exception{
		com.sun.image.codec.jpeg.JPEGImageEncoder jie=com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(out);
		Image image = processImage(imageSource,baseWidth,baseHeight);
		jie.encode((BufferedImage)image);
	}

}
