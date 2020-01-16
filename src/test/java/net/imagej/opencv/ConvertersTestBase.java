package net.imagej.opencv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;

import org.apache.commons.io.FileUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.opencv.core.CvType;

import io.scif.SCIFIO;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.RandomAccessibleInterval;

public abstract class ConvertersTestBase {

	//protected static final String TEST_DIR = System.getProperty( "tmp.dir" ) + File.separator + "opencv-tests";
	protected static final String TEST_DIR = "/Users/turek/Desktop/test";
	protected static final String input = TEST_DIR + File.separator + "input.tif";
	protected static final String mat2img = TEST_DIR + File.separator + "mat2img.tif";
	protected static final String img2mat = TEST_DIR + File.separator + "img2mat.tif";
	protected static final String fullCircleFromMat = TEST_DIR + File.separator + "fullCircleFromMat.tif";
	protected static final String fullCircleFromImg = TEST_DIR + File.separator + "fullCircleFromImg.tif";
	protected static SCIFIO scifio;
	protected static boolean DEBUG = true;

	@BeforeClass
	public static void setup() {
		File testDir = new File( TEST_DIR );
		try {
			if ( testDir.exists() ) {
				FileUtils.cleanDirectory( testDir );
			} else {
				Files.createDirectory( testDir.toPath(), new FileAttribute< ? >[ 0 ] );
			}
		} catch ( IOException e ) {
			fail( e.getMessage() );
		}
	}

	@AfterClass
	public static void cleanup() throws IOException {
		if ( !DEBUG ) {
			FileUtils.forceDelete( new File( TEST_DIR ) );
		}
	}

	protected void checkData( Mat mat1, Mat mat2 ) {
		int type = mat1.depth();
		switch ( type ) {
		case CvType.CV_8U:
		case CvType.CV_8S:
			byte[] byteData1 = MatToImgConverter.toByteArray( mat1 );
			byte[] byteData2 = MatToImgConverter.toByteArray( mat2 );

			if ( DEBUG ) {
				for ( int i = 0; i < byteData1.length; i++ ) {
					System.out.println( "i = " + i + " , mat1 = " + byteData1[ i ] + " , mat2 = " + byteData2[ i ] );
				}
			}

			assertEquals( byteData1.length, byteData2.length );
			Assert.assertArrayEquals( byteData1, byteData2 );
			break;
		case CvType.CV_32S:
			int[] intData1 = MatToImgConverter.toIntArray( mat1 );
			int[] intData2 = MatToImgConverter.toIntArray( mat2 );

			assertEquals( intData1.length, intData2.length );
			Assert.assertArrayEquals( intData1, intData2 );
			break;
		case CvType.CV_32F:
			float[] matData1 = MatToImgConverter.toFloatArray( mat1 );
			float[] matData2 = MatToImgConverter.toFloatArray( mat2 );

			assertEquals( matData1.length, matData2.length );
			Assert.assertArrayEquals( matData1, matData2, 0.01f );
			break;
		case CvType.CV_64F:
			double[] dblData1 = MatToImgConverter.toDoubleArray( mat1 );
			double[] dblData2 = MatToImgConverter.toDoubleArray( mat2 );

			assertEquals( dblData1.length, dblData2.length );
			Assert.assertArrayEquals( dblData1, dblData2, 0.01 );
			break;
		default:
			throw new UnsupportedOperationException( "Unsupported CvType value: " + type );
		}
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	protected static void saveImg( RandomAccessibleInterval image, String output ) {

		Dataset ds = getScifio().getContext().getService( DatasetService.class ).create( image );
		try {
			getScifio().datasetIO().save( ds, output );
		} catch ( IOException e ) {
			fail( e.getMessage() );
		}
	}

	protected static SCIFIO getScifio() {
		if ( scifio == null )
			scifio = new SCIFIO();
		return scifio;
	}
}
