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
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * Note: smaller test set as the openCV imread does not handle
 * 32bit and higher inputs. Also imwrite creates only 8bit outputs
 * @author turek
 *
 */
public class DoubleImageConvertersTest extends ConvertersTestBase {

	private static ArrayImg< DoubleType, DoubleArray > inputImg;

	@BeforeClass
	public static void init() {

		setup ();
		
		inputImg = createLargeRectangularImage();
		saveImg( inputImg, input );
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testImgToMatConversion() throws IOException {
		//Read image with ImageJ and convert to openCV
		Dataset dataset = getScifio().datasetIO().open( input );
		Mat convertedImg = new ImgToMatConverter().convert( ( RandomAccessibleInterval< DoubleType > ) dataset.getImgPlus().getImg(), Mat.class );
		opencv_imgcodecs.imwrite( img2mat, convertedImg );

		//Compare data
		checkData( inputImg, convertedImg );
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testFullCircleConversionFromIJ() throws IOException {

		Dataset dataset = getScifio().datasetIO().open( input );
		RandomAccessibleInterval< DoubleType > image =
				RealTypeConverters.convert( ( RandomAccessibleInterval< ? extends RealType< ? > > ) dataset.getImgPlus().getImg(), new DoubleType() );
		Mat cvMat = new ImgToMatConverter().convert( image, Mat.class );
		RandomAccessibleInterval< DoubleType > cvImg = ( RandomAccessibleInterval< DoubleType > ) MatToImgConverter.convert( cvMat );
		saveImg( cvImg, fullCircleFromImg );

		checkData( cvImg, inputImg );

	}

	private void checkData( RandomAccessibleInterval< DoubleType > img, Mat mat ) {
		final double[] matData = MatToImgConverter.toDoubleArray( mat );
		final double[] imgData = ImgToMatConverter.toDoubleArray( ( RandomAccessibleInterval< DoubleType > ) img );
		assertEquals( matData.length, imgData.length );
		Assert.assertArrayEquals( matData, imgData, 0.01 );
	}

	private void checkData( RandomAccessibleInterval< DoubleType > img1, RandomAccessibleInterval< DoubleType > img2 ) {
		final double[] imgData1 = ImgToMatConverter.toDoubleArray( ( RandomAccessibleInterval< DoubleType > ) img1 );
		final double[] imgData2 = ImgToMatConverter.toDoubleArray( ( RandomAccessibleInterval< DoubleType > ) img2 );

		assertEquals( imgData1.length, imgData2.length );
		Assert.assertArrayEquals( imgData1, imgData2, 0.01 );
	}

	/**
	 * Creates a large rectangular (1024 x 512) striped image
	 */
	private static ArrayImg< DoubleType, DoubleArray > createLargeRectangularImage() {
		ArrayImg< DoubleType, DoubleArray > img = ArrayImgs.doubles( 1024, 512 );
		for ( int i = 0; i < 3; i++ ) {
			long[] xmin = new long[] { 199 + i * 200, 0 };
			long[] xmax = new long[] { 299 + i * 200, 511 };
			IntervalView< DoubleType > iview = Views.interval( img, xmin, xmax );
			iview.forEach( pixel -> pixel.set( ( int ) 255 ) );
		}
		return img;
	}

}
