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
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * Note: smaller test set as the openCV imread does not handle
 * 32bit and higher inputs. Also imwrite creates only 8bit outputs
 * @author turek
 *
 */
public class FloatImageConvertersTest extends ConvertersTestBase {

	private static ArrayImg< FloatType, FloatArray > inputImg;

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
		Mat convertedImg = new ImgToMatConverter().convert( ( RandomAccessibleInterval< FloatType > ) dataset.getImgPlus().getImg(), Mat.class);
		opencv_imgcodecs.imwrite( img2mat, convertedImg );

		//Compare data
		checkData( inputImg, convertedImg );
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testFullCircleConversionFromIJ() throws IOException {

		Dataset dataset = getScifio().datasetIO().open( input );
		RandomAccessibleInterval< FloatType > image =
				RealTypeConverters.convert( ( RandomAccessibleInterval< ? extends RealType< ? > > ) dataset.getImgPlus().getImg(), new FloatType() );
		Mat cvMat = new ImgToMatConverter().convert( image, Mat.class );
		RandomAccessibleInterval< FloatType > cvImg = new MatToImgConverter().convert( cvMat, RandomAccessibleInterval.class );
		saveImg( cvImg, fullCircleFromImg );

		checkData( cvImg, inputImg );

	}

	private void checkData( RandomAccessibleInterval< FloatType > img, Mat mat ) {
		final float[] matData = MatToImgConverter.toFloatArray( mat );
		final float[] imgData = ImgToMatConverter.toFloatArray( ( RandomAccessibleInterval< FloatType > ) img );
		assertEquals( matData.length, imgData.length );
		Assert.assertArrayEquals( matData, imgData, 0.01f );
	}

	private void checkData( RandomAccessibleInterval< FloatType > img1, RandomAccessibleInterval< FloatType > img2 ) {
		final float[] imgData1 = ImgToMatConverter.toFloatArray( ( RandomAccessibleInterval< FloatType > ) img1 );
		final float[] imgData2 = ImgToMatConverter.toFloatArray( ( RandomAccessibleInterval< FloatType > ) img2 );

		assertEquals( imgData1.length, imgData2.length );
		Assert.assertArrayEquals( imgData1, imgData2, 0.01f );
	}

	/**
	 * Creates a large rectangular (1024 x 512) striped image
	 */
	private static ArrayImg< FloatType, FloatArray > createLargeRectangularImage() {
		ArrayImg< FloatType, FloatArray > img = ArrayImgs.floats( 1024, 512 );
		for ( int i = 0; i < 3; i++ ) {
			long[] xmin = new long[] { 199 + i * 200, 0 };
			long[] xmax = new long[] { 299 + i * 200, 511 };
			IntervalView< FloatType > iview = Views.interval( img, xmin, xmax );
			iview.forEach( pixel -> pixel.set( ( int ) 255 ) );
		}
		return img;
	}

}
