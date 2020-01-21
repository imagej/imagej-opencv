package net.imagej.opencv;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.imagej.Dataset;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * Note: smaller test set as the openCV imread does not handle
 * 32bit and higher inputs. Also imwrite creates only 8bit outputs
 * @author turek
 *
 */
public class IntImageConvertersTest extends ConvertersTestBase {

	private static ArrayImg< IntType, IntArray > inputImg;

	@BeforeClass
	public static void init() {
		
		setup();

		inputImg = createLargeRectangularImage();
		saveImg( inputImg, input );
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testImgToMatConversion() throws IOException {
		//Read image with ImageJ and convert to openCV
		Dataset dataset = getScifio().datasetIO().open( input );
		Mat convertedImg = new ImgToMatConverter().convert( ( RandomAccessibleInterval< IntType > ) dataset.getImgPlus().getImg(), Mat.class );
		opencv_imgcodecs.imwrite( img2mat, convertedImg );

		//Compare data
		checkData( inputImg, convertedImg );
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testFullCircleConversionFromIJ() throws IOException {

		Dataset dataset = getScifio().datasetIO().open( input );
		RandomAccessibleInterval< IntType > image =
				RealTypeConverters.convert( ( RandomAccessibleInterval< ? extends RealType< ? > > ) dataset.getImgPlus().getImg(), new IntType() );
		Mat cvMat = new ImgToMatConverter().convert( image, Mat.class );
		RandomAccessibleInterval< IntType > cvImg = ( RandomAccessibleInterval< IntType > ) MatToImgConverter.convert( cvMat );
		saveImg( cvImg, fullCircleFromImg );

		checkData( cvImg, inputImg );

	}

	private void checkData( RandomAccessibleInterval< IntType > img, Mat mat ) {
		final int[] matData = MatToImgConverter.toIntArray( mat );
		final int[] imgData = ImgToMatConverter.toIntArray( ( RandomAccessibleInterval< IntType > ) img );
		assertEquals( matData.length, imgData.length );
		Assert.assertArrayEquals( matData, imgData );
	}

	private void checkData( RandomAccessibleInterval< IntType > img1, RandomAccessibleInterval< IntType > img2 ) {
		final int[] imgData1 = ImgToMatConverter.toIntArray( ( RandomAccessibleInterval< IntType > ) img1 );
		final int[] imgData2 = ImgToMatConverter.toIntArray( ( RandomAccessibleInterval< IntType > ) img2 );

		assertEquals( imgData1.length, imgData2.length );
		Assert.assertArrayEquals( imgData1, imgData2 );
	}

	/**
	 * Creates a large rectangular (1024 x 512) striped image
	 */
	private static ArrayImg< IntType, IntArray > createLargeRectangularImage() {
		ArrayImg< IntType, IntArray > img = ArrayImgs.ints( 1024, 512 );
		for ( int i = 0; i < 3; i++ ) {
			long[] xmin = new long[] { 199 + i * 200, 0 };
			long[] xmax = new long[] { 299 + i * 200, 511 };
			IntervalView< IntType > iview = Views.interval( img, xmin, xmax );
			iview.forEach( pixel -> pixel.set( ( int ) 255 ) );
		}
		return img;
	}

}
