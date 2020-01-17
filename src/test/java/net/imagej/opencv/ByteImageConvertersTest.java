package net.imagej.opencv;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class ByteImageConvertersTest extends ConvertersTestBase {

	@BeforeClass
	public static void createTestImage() {

		ArrayImg< UnsignedByteType, ByteArray > img = createLargeRectangularImage();
		saveImg( img, input );
	}

	@Test
	public void testMatAndImgArraysAreSameShape() throws IOException {

		// Note: if do not specify grayscale codec the resulting Mat will have more than one channel.
		Mat mat = imread( input, opencv_imgcodecs.IMREAD_GRAYSCALE );
		if ( mat.empty() )
			fail( "Couldn't load image: " + input );

		Dataset dataset = getScifio().datasetIO().open( input );
		RandomAccessibleInterval< ByteType > image =
				RealTypeConverters.convert( ( RandomAccessibleInterval< ? extends RealType< ? > > ) dataset.getImgPlus().getImg(), new ByteType() );
		checkData( image, mat );

	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testMatToImgConversion() throws IOException {
		//Read image with openCV and convert to ImageJ
		Mat mat = imread( input, opencv_imgcodecs.IMREAD_GRAYSCALE );
		if ( mat.empty() )
			fail( "Couldn't load image: " + input );
		RandomAccessibleInterval< ByteType > convertedMat = ( RandomAccessibleInterval< ByteType > ) MatToImgConverter.toImg( mat );
		saveImg( convertedMat, mat2img );

		//Read image with ImageJ
		Dataset dataset = getScifio().datasetIO().open( input );
		RandomAccessibleInterval< ByteType > ijImage =
				RealTypeConverters.convert( ( RandomAccessibleInterval< ? extends RealType< ? > > ) dataset.getImgPlus().getImg(), new ByteType() );

		//Compare data
		checkData( convertedMat, ijImage );
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testImgToMatConversion() throws IOException {
		//Read image with ImageJ and convert to openCV
		Dataset dataset = getScifio().datasetIO().open( input );
		Mat convertedImg = ImgToMatConverter.toMat( ( RandomAccessibleInterval< UnsignedByteType > ) dataset.getImgPlus().getImg() );
		opencv_imgcodecs.imwrite( img2mat, convertedImg );

		//Read image with OpenCV
		Mat mat = imread( input, opencv_imgcodecs.IMREAD_GRAYSCALE );
		if ( mat.empty() )
			fail( "Couldn't load image: " + input );

		//Compare data
		checkData( convertedImg, mat );
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testFullCircleConversionFromMat() throws IOException {

		Mat mat = imread( input, opencv_imgcodecs.IMREAD_GRAYSCALE );
		if ( mat.empty() )
			fail( "Couldn't load image: " + input );

		RandomAccessibleInterval< UnsignedByteType > cvImg = ( RandomAccessibleInterval< UnsignedByteType > ) MatToImgConverter.toImg( mat );
		Mat cvMat = ImgToMatConverter.toMat( cvImg );
		opencv_imgcodecs.imwrite( fullCircleFromMat, cvMat );
		checkData( cvMat, mat );
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testFullCircleConversionFromIJ() throws IOException {

		Dataset dataset = getScifio().datasetIO().open( input );
		RandomAccessibleInterval< ByteType > image =
				RealTypeConverters.convert( ( RandomAccessibleInterval< ? extends RealType< ? > > ) dataset.getImgPlus().getImg(), new ByteType() );
		Mat cvMat = ImgToMatConverter.toMat( image );
		RandomAccessibleInterval< ByteType > cvImg = ( RandomAccessibleInterval< ByteType > ) MatToImgConverter.toImg( cvMat );
		saveImg( cvImg, fullCircleFromImg );

		checkData( cvImg, image );

	}

	private void checkData( RandomAccessibleInterval< ByteType > img, Mat mat ) {
		final byte[] matData = MatToImgConverter.toByteArray( mat );
		final byte[] imgData = ImgToMatConverter.toByteArray( ( RandomAccessibleInterval< ByteType > ) img );
		assertEquals( matData.length, imgData.length );
		Assert.assertArrayEquals( matData, imgData );
	}

	private void checkData( RandomAccessibleInterval< ByteType > img1, RandomAccessibleInterval< ByteType > img2 ) {
		final byte[] imgData1 = ImgToMatConverter.toByteArray( ( RandomAccessibleInterval< ByteType > ) img1 );
		final byte[] imgData2 = ImgToMatConverter.toByteArray( ( RandomAccessibleInterval< ByteType > ) img2 );

		assertEquals( imgData1.length, imgData2.length );
		Assert.assertArrayEquals( imgData1, imgData2 );
	}

	/**
	 * Creates a large rectangular (1024 x 512) striped image
	 */
	private static ArrayImg< UnsignedByteType, ByteArray > createLargeRectangularImage() {
		ArrayImg< UnsignedByteType, ByteArray > img = ArrayImgs.unsignedBytes( 1024, 512 );
		for ( int i = 0; i < 3; i++ ) {
			long[] xmin = new long[] { 199 + i * 200, 0 };
			long[] xmax = new long[] { 299 + i * 200, 511 };
			IntervalView< UnsignedByteType > iview = Views.interval( img, xmin, xmax );
			iview.forEach( pixel -> pixel.set( ( byte ) 255 ) );
		}
		return img;
	}

}
