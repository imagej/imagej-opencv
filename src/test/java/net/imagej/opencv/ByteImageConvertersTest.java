/*-
 * #%L
 * ImageJ/OpenCV Integration
 * %%
 * Copyright (C) 2019 - 2024 ImageJ2 developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
	public static void init() {

		setup();
		
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
		RandomAccessibleInterval< ByteType > convertedMat = ( RandomAccessibleInterval< ByteType > ) new MatToImgConverter().convert( mat, RandomAccessibleInterval.class );
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
		Mat convertedImg = new ImgToMatConverter().convert( ( RandomAccessibleInterval< UnsignedByteType > ) dataset.getImgPlus().getImg(), Mat.class );
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

		RandomAccessibleInterval< UnsignedByteType > cvImg = ( RandomAccessibleInterval< UnsignedByteType > ) MatToImgConverter.convert( mat );
		Mat cvMat = new ImgToMatConverter().convert( cvImg, Mat.class );
		opencv_imgcodecs.imwrite( fullCircleFromMat, cvMat );
		checkData( cvMat, mat );
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testFullCircleConversionFromIJ() throws IOException {

		Dataset dataset = getScifio().datasetIO().open( input );
		RandomAccessibleInterval< ByteType > image =
				RealTypeConverters.convert( ( RandomAccessibleInterval< ? extends RealType< ? > > ) dataset.getImgPlus().getImg(), new ByteType() );
		Mat cvMat = new ImgToMatConverter().convert( image, Mat.class );
		RandomAccessibleInterval< ByteType > cvImg = ( RandomAccessibleInterval< ByteType > ) new MatToImgConverter().convert( cvMat, RandomAccessibleInterval.class );
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
