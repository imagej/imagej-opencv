package net.imagej.opencv;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imreadmulti;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.junit.Test;

import ij.IJ;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.opencv.ImgToMatConverter;
import net.imagej.opencv.MatToImgConverter;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImgToVirtualStack;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.view.Views;

public class ConvertersTest {

	private final static String input = "/Users/turek/Downloads/MAX_2018001_LP823_Control-03-02_Myosin_denoised.tif";

	@Test
	public void testMatAndImgArraysAreSameShape() throws IOException {

		MatVector mats = new MatVector();
		imreadmulti( input, mats );
		if ( mats.empty() )
			throw new IOException( "Couldn't load image: " + input );
		ImageJ ij = new ImageJ();
		Dataset dataset = ij.get( DatasetIOService.class ).open( input );
		RandomAccessibleInterval< ByteType > image =
				RealTypeConverters.convert( ( RandomAccessibleInterval< ? extends RealType< ? > > ) dataset.getImgPlus().getImg(), new ByteType() );
		for ( int i = 0; i < 60; i++ ) {
			Mat matSlice = mats.get( i );
			opencv_imgcodecs.imwrite( "/Users/turek/Desktop/test/matSlice" + i + ".tif", matSlice );
			RandomAccessibleInterval< ByteType > ijSlice = Views.hyperSlice( image, 2, i );
			IJ.save( ImgToVirtualStack.wrap( toImgPlus( ijSlice ) ), "/Users/turek/Desktop/test/ijSlice" + i + ".tif" );
			checkData( ijSlice, matSlice );
		}

	}

	@Test
	public void testFullCircleConversionFromIJ() throws IOException {

		MatVector mats = new MatVector();
		imreadmulti( input, mats );
		if ( mats.empty() )
			throw new IOException( "Couldn't load image: " + input );
		ImageJ ij = new ImageJ();
		Dataset dataset = ij.get( DatasetIOService.class ).open( input );
		RandomAccessibleInterval< ByteType > image =
				RealTypeConverters.convert( ( RandomAccessibleInterval< ? extends RealType< ? > > ) dataset.getImgPlus().getImg(), new ByteType() );
		for ( int i = 0; i < 60; i++ ) {
			RandomAccessibleInterval< ByteType > ijSlice = Views.hyperSlice( image, 2, i );
			IJ.save( ImgToVirtualStack.wrap( toImgPlus( ijSlice ) ), "/Users/turek/Desktop/test/ijSlice" + i + ".tif" );
			Mat mat = ImgToMatConverter.matByte( ijSlice );
			RandomAccessibleInterval< ByteType > newijSlice = MatToImgConverter.imgByte( mat );
			IJ.save( ImgToVirtualStack.wrap( toImgPlus( newijSlice ) ), "/Users/turek/Desktop/test/newijSlice" + i + ".tif" );
			checkData( newijSlice, ijSlice );

		}

	}

	@Test
	public void testFullCircleConversionFromMat() throws IOException {

		MatVector mats = new MatVector();
		imreadmulti( input, mats );
		if ( mats.empty() )
			throw new IOException( "Couldn't load image: " + input );
		for ( int i = 0; i < 60; i++ ) {
			Mat matSlice = mats.get( i );
			opencv_imgcodecs.imwrite( "/Users/turek/Desktop/test/matSlice" + i + ".tif", matSlice );
			RandomAccessibleInterval< ByteType > img = MatToImgConverter.imgByte( matSlice );
			Mat newMat = ImgToMatConverter.matByte( img );
			opencv_imgcodecs.imwrite( "/Users/turek/Desktop/test/newMat" + i + ".tif", newMat );
			checkData( newMat, matSlice );
		}

	}

	@Test
	public void testMatToImgComversion() throws IOException {
		MatVector mats = new MatVector();
		imreadmulti( input, mats );
		if ( mats.empty() )
			throw new IOException( "Couldn't load image: " + input );
		ImageJ ij = new ImageJ();
		Dataset dataset = ij.get( DatasetIOService.class ).open( input );
		RandomAccessibleInterval< ByteType > image =
				RealTypeConverters.convert( ( RandomAccessibleInterval< ? extends RealType< ? > > ) dataset.getImgPlus().getImg(), new ByteType() );
		for ( int i = 0; i < 60; i++ ) {
			Mat matSlice = mats.get( i );
			RandomAccessibleInterval< ByteType > cvMatSlice = MatToImgConverter.imgByte( matSlice );
			IJ.save( ImgToVirtualStack.wrap( toImgPlus( cvMatSlice ) ), "/Users/turek/Desktop/test/cvMatSlice" + i + ".tif" );
			RandomAccessibleInterval< ByteType > ijSlice = Views.hyperSlice( image, 2, i );
			checkData( cvMatSlice, ijSlice );
		}

	}

	@Test
	public void testImgToMatConversion() throws IOException {
		MatVector mats = new MatVector();
		imreadmulti( input, mats );
		if ( mats.empty() )
			throw new IOException( "Couldn't load image: " + input );
		ImageJ ij = new ImageJ();
		Dataset dataset = ij.get( DatasetIOService.class ).open( input );
		RandomAccessibleInterval< ByteType > image =
				RealTypeConverters.convert( ( RandomAccessibleInterval< ? extends RealType< ? > > ) dataset.getImgPlus().getImg(), new ByteType() );
		for ( int i = 0; i < 60; i++ ) {
			RandomAccessibleInterval< ByteType > ijSlice = Views.hyperSlice( image, 2, i );
			Mat cvImgSlice = ImgToMatConverter.matByte( ijSlice );
			opencv_imgcodecs.imwrite( "/Users/turek/Desktop/test/cvImgSlice" + i + ".tif", cvImgSlice );
			checkData( cvImgSlice, mats.get( i ) );
		}

	}

	private void checkData( RandomAccessibleInterval< ByteType > img, Mat mat ) {
		final byte[] matData = MatToImgConverter.getMatDataAsByteArray( mat );
		final byte[] imgData = ImgToMatConverter.byteArray( img );

		assertEquals( matData.length, imgData.length );
		org.junit.Assert.assertArrayEquals( matData, imgData );
	}

	private void checkData( RandomAccessibleInterval< ByteType > img1, RandomAccessibleInterval< ByteType > img2 ) {
		final byte[] imgData1 = ImgToMatConverter.byteArray( img1 );
		final byte[] imgData2 = ImgToMatConverter.byteArray( img2 );

		assertEquals( imgData1.length, imgData2.length );
		org.junit.Assert.assertArrayEquals( imgData1, imgData2 );
	}

	private void checkData( Mat mat1, Mat mat2 ) {
		final byte[] matData1 = MatToImgConverter.getMatDataAsByteArray( mat1 );
		final byte[] matData2 = MatToImgConverter.getMatDataAsByteArray( mat2 );

		assertEquals( matData1.length, matData2.length );
		org.junit.Assert.assertArrayEquals( matData1, matData2 );
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private static ImgPlus< ? > toImgPlus( RandomAccessibleInterval< ? > image ) {
		if ( image instanceof ImgPlus )
			return ( ImgPlus< ? > ) image;
		if ( image instanceof Img )
			return new ImgPlus<>( ( Img< ? > ) image );
		return new ImgPlus<>( ImgView.wrap( ( RandomAccessibleInterval ) image, null ) );
	}

}
