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

import static org.bytedeco.opencv.global.opencv_imgcodecs.imreadmulti;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;

import io.scif.services.DatasetIOService;
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

public class StackedImageTest extends ConvertersTestBase {

	@BeforeClass
	public static void init() {
		setup();

		ArrayImg< UnsignedByteType, ByteArray > img = createLargeRectangularImage();
		saveImg( img, input );
	}

	private Context ctx;

	@Before
	public void setUp() {
		ctx = new Context();
	}

	@After
	public void tearDown() {
		ctx.dispose();
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testFullCircleConversionFromIJ() throws IOException {
		Dataset dataset = ctx.service( DatasetIOService.class ).open( input );
		RandomAccessibleInterval< ByteType > image =
				RealTypeConverters.convert( ( RandomAccessibleInterval< ? extends RealType< ? > > ) dataset.getImgPlus().getImg(), new ByteType() );
		
		MatVector cvMatImg = ( MatVector ) new ImgToMatVectorConverter().convert( image, MatVector.class );
		RandomAccessibleInterval< ByteType > cvImg = (RandomAccessibleInterval< ByteType >) new MatVectorToImgConverter().convert(cvMatImg, RandomAccessibleInterval.class);
		saveImg(cvImg, fullCircleFromImg );
		
		for ( int i = 0; i < 3; i++ ) {
			saveImg(Views.hyperSlice( cvImg, 2, i ), TEST_DIR + File.separator + "img2imgSlice" + i + ".tif" );
			checkData(Views.hyperSlice( cvImg, 2, i ), Views.hyperSlice( image, 2, i ));
		}
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testFullCircleConversionFromMatVector() throws IOException {

		MatVector mats = new MatVector();
		imreadmulti( input, mats, opencv_imgcodecs.IMREAD_GRAYSCALE );
		if ( mats.empty() )
			throw new IOException( "Couldn't load image: " + input );
		
		RandomAccessibleInterval< ByteType > cvMats = (RandomAccessibleInterval< ByteType >) new MatVectorToImgConverter().convert(mats, RandomAccessibleInterval.class);
		MatVector cvImgMat = ( MatVector ) new ImgToMatVectorConverter().convert( cvMats, MatVector.class );
		
		for ( int i = 0; i < 3; i++ ) {
			opencv_imgcodecs.imwrite( TEST_DIR + File.separator + "mv2mvSlice" + i + ".tif", cvImgMat.get(i) );
			checkData( mats.get( i ), cvImgMat.get(i) );
		}

	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testMatVectorToImgComversion() throws IOException {
		MatVector mats = new MatVector();
		imreadmulti( input, mats, opencv_imgcodecs.IMREAD_GRAYSCALE );
		if ( mats.empty() )
			throw new IOException( "Couldn't load image: " + input );
		
		RandomAccessibleInterval< ByteType > cvMats = (RandomAccessibleInterval< ByteType >) new MatVectorToImgConverter().convert(mats, RandomAccessibleInterval.class);
		saveImg(cvMats, mat2img);
		
		for ( int i = 0; i < 3; i++ ) {
			saveImg(Views.hyperSlice( cvMats, 2, i ), TEST_DIR + File.separator + "mv2imgSlice" + i + ".tif" );
			checkData( (RandomAccessibleInterval< ByteType >)Views.hyperSlice( cvMats, 2, i ), mats.get(i));
		}

	}

	@Test
	public void testImgToMatVectorConversion() throws IOException {
		Dataset dataset = ctx.service( DatasetIOService.class ).open( input );
		RandomAccessibleInterval< ByteType > image =
				RealTypeConverters.convert( ( RandomAccessibleInterval< ? extends RealType< ? > > ) dataset.getImgPlus().getImg(), new ByteType() );
		
		MatVector cvImg = new ImgToMatVectorConverter().convert( image, MatVector.class );
		
		for ( int i = 0; i < 3; i++ ) {
			opencv_imgcodecs.imwrite( TEST_DIR + File.separator + "img2mvSlice" + i + ".tif", cvImg.get( i ) );
			checkData( Views.hyperSlice( image, 2, i ), cvImg.get( i ) );
		}

	}

	private void checkData( RandomAccessibleInterval< ByteType > img, Mat mat ) {
		final byte[] matData = MatToImgConverter.toByteArray( mat );
		final byte[] imgData = ImgToMatConverter.toByteArray( img );

		assertEquals( matData.length, imgData.length );
		Assert.assertArrayEquals( matData, imgData );
	}

	private void checkData( RandomAccessibleInterval< ByteType > img1, RandomAccessibleInterval< ByteType > img2 ) {
		final byte[] imgData1 = ImgToMatConverter.toByteArray( img1 );
		final byte[] imgData2 = ImgToMatConverter.toByteArray( img2 );

		assertEquals( imgData1.length, imgData2.length );
		Assert.assertArrayEquals( imgData1, imgData2 );
	}

	/**
	 * Creates a large rectangular (1024 x 512 x3) stacked striped image
	 */
	private static ArrayImg< UnsignedByteType, ByteArray > createLargeRectangularImage() {
		ArrayImg< UnsignedByteType, ByteArray > img = ArrayImgs.unsignedBytes( 1024, 512, 3 );
		for ( int j = 0; j < 3; j++ ) {
			for ( int i = 0; i < 3; i++ ) {
				long[] xmin = new long[] { ( 199 + j * 50 ) + i * 200, 0, j };
				long[] xmax = new long[] { ( 299 + j * 50 ) + i * 200, 511, j };
				IntervalView< UnsignedByteType > iview = Views.interval( img, xmin, xmax );
				iview.forEach( pixel -> pixel.set( ( byte ) 255 ) );
			}
		}
		return img;
	}

}
