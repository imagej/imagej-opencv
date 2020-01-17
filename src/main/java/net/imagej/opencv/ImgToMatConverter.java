package net.imagej.opencv;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.opencv.core.CvType;
import org.scijava.Prioritized;
import org.scijava.Priority;
import org.scijava.convert.AbstractConverter;
import org.scijava.convert.Converter;
import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;

/**
 * @author G.Turek for OpenCV version 4.1.2
 *         Only 3 dimensional, 1 channel images are supported for the moment
 */

@SuppressWarnings( "rawtypes" )
@Plugin( type = Converter.class, priority = Priority.LOW )
public class ImgToMatConverter extends AbstractConverter< RandomAccessibleInterval, Mat > {

	@Override
	public int compareTo( Prioritized o ) {
		return super.compareTo( o );
	}

	@Override
	public LogService log() {
		return super.log();
	}

	@Override
	public String getIdentifier() {
		return super.getIdentifier();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public < T > T convert( Object o, Class< T > type ) {
		RandomAccessibleInterval< T > imp = ( RandomAccessibleInterval< T > ) o;
		return ( T ) toMat( imp );

	}

	@Override
	public Class< Mat > getOutputType() {
		return Mat.class;
	}

	@Override
	public Class< RandomAccessibleInterval > getInputType() {
		return RandomAccessibleInterval.class;
	}

	/**
	 * Creates am OpenCV Mat matrix containing data from the given image.
	 * 
	 * @param <T>
	 * 
	 * @param image
	 *            The image which should be put into the mat.
	 * @return A mat containing the data of the image.
	 * @throws IllegalArgumentException
	 *             if the type of the image is not supported.
	 *             Supported types are {@link ByteType}, {@link DoubleType},
	 *             {@link FloatType} and {@link IntType}
	 */
	@SuppressWarnings( "unchecked" )
	public static < T > Mat toMat(
			final RandomAccessibleInterval< T > image ) {
		if ( image.numDimensions() > 3 )
			throw new IllegalArgumentException( "Images with more than 3 dimensions are not supported yet" );
		final T type = Util.getTypeFromInterval( image );
		if ( type instanceof UnsignedByteType ) {
			return getUnsignedByteMat( ( RandomAccessibleInterval< UnsignedByteType > ) image );

		}
		if ( type instanceof ByteType ) { return getByteMat( ( RandomAccessibleInterval< ByteType > ) image ); }
		if ( type instanceof IntType ) { return getIntMat( ( RandomAccessibleInterval< IntType > ) image ); }
		if ( type instanceof FloatType ) { return getFloatMat( ( RandomAccessibleInterval< FloatType > ) image ); }
		if ( type instanceof DoubleType ) { return getDoubleMat( ( RandomAccessibleInterval< DoubleType > ) image ); }
		throw new IllegalArgumentException( "Unsupported image type: " + type.getClass().getName() );
	}

	/**
	 * Creates an OpenCV Mat matrix containing data from the given byte image.
	 * 
	 * @param image
	 *            The image which should be put into the Mat.
	 * @return A Mat containing the data of the image.
	 */
	public static Mat getUnsignedByteMat(
			final RandomAccessibleInterval< UnsignedByteType > image ) {
		int[] shape = Intervals.dimensionsAsIntArray( image );
		byte[] data = toUByteArray( image );
		return createByteMat( shape, CvType.CV_8UC1, data );

	}

	/**
	 * Creates an OpenCV Mat matrix containing data from the given byte image.
	 * 
	 * @param image
	 *            The image which should be put into the Mat.
	 * @return A Mat containing the data of the image.
	 */
	public static Mat getByteMat(
			final RandomAccessibleInterval< ByteType > image ) {
		int[] shape = Intervals.dimensionsAsIntArray( image );
		byte[] data = toByteArray( image );
		return createByteMat( shape, CvType.CV_8SC1, data );
	}

	private static Mat createByteMat( final int[] shape, final int cvType, final byte[] data ) {
		// We need to invert X and Y in order to get the right orientation.
		if ( shape.length == 2 ) {
			return new Mat( shape[ 1 ], shape[ 0 ], cvType, new BytePointer( data ) );
		} else {
			int[] reshape = shape.clone();
			reshape[ 0 ] = shape[ 1 ];
			reshape[ 1 ] = shape[ 0 ];
			return new Mat( reshape.length, reshape, cvType, new BytePointer( data ) );
		}
	}

	/**
	 * Creates an OpenCV Mat matrix containing data from the given int image.
	 * 
	 * @param image
	 *            The image which should be put into the Mat.
	 * @return A Mat containing the data of the image.
	 */
	public static Mat getIntMat(
			final RandomAccessibleInterval< IntType > image ) {
		int[] shape = Intervals.dimensionsAsIntArray( image );
		int[] data = toIntArray( image );
		return createIntMat( shape, data );
	}

	private static Mat createIntMat( final int[] shape, final int[] data ) {
		// We need to invert X and Y in order to get the right orientation.
		if ( shape.length == 2 ) {
			return new Mat( shape[ 1 ], shape[ 0 ], CvType.CV_32SC1, new IntPointer( data ) );
		} else {
			int[] reshape = shape.clone();
			reshape[ 0 ] = shape[ 1 ];
			reshape[ 1 ] = shape[ 0 ];
			return new Mat( reshape.length, reshape, CvType.CV_32SC1, new IntPointer( data ) );
		}
	}

	/**
	 * Creates an OpenCV Mat matrix containing data from the given float image.
	 * 
	 * @param image
	 *            The image which should be put into the Mat.
	 * @return A Mat containing the data of the image.
	 */
	public static Mat getFloatMat(
			final RandomAccessibleInterval< FloatType > image ) {
		int[] shape = Intervals.dimensionsAsIntArray( image );
		float[] data = toFloatArray( image );
		return createFloatMat( shape, data );
	}

	private static Mat createFloatMat( final int[] shape, final float[] data ) {
		// We need to invert X and Y in order to get the right orientation.
		if ( shape.length == 2 ) {
			return new Mat( shape[ 1 ], shape[ 0 ], CvType.CV_32FC1, new FloatPointer( data ) );
		} else {
			int[] reshape = shape.clone();
			reshape[ 0 ] = shape[ 1 ];
			reshape[ 1 ] = shape[ 0 ];
			return new Mat( reshape.length, reshape, CvType.CV_32FC1, new FloatPointer( data ) );
		}
	}

	/**
	 * Creates an OpenCV Mat matrix containing data from the given double image.
	 * 
	 * @param image
	 *            The image which should be put into the Mat.
	 * @return A Mat containing the data of the image.
	 */
	public static Mat getDoubleMat(
			final RandomAccessibleInterval< DoubleType > image ) {
		int[] shape = Intervals.dimensionsAsIntArray( image );
		double[] data = toDoubleArray( image );
		return createDoubleMat( shape, data );
	}

	private static Mat createDoubleMat( final int[] shape, final double[] data ) {
		// We need to invert X and Y in order to get the right orientation.
		if ( shape.length == 2 ) {
			return new Mat( shape[ 1 ], shape[ 0 ], CvType.CV_64FC1, new DoublePointer( data ) );
		} else {
			int[] reshape = shape.clone();
			reshape[ 0 ] = shape[ 1 ];
			reshape[ 1 ] = shape[ 0 ];
			return new Mat( reshape.length, reshape, CvType.CV_64FC1, new DoublePointer( data ) );
		}
	}

	public static byte[] toUByteArray( RandomAccessibleInterval< UnsignedByteType > image ) {
		byte[] outputArray = new byte[ ( int ) Intervals.numElements( image ) ];
		long[] shape = Intervals.dimensionsAsLongArray( image );
		copyFromTo( image, ArrayImgs.unsignedBytes( outputArray, shape ) );
		return outputArray;
	}

	public static byte[] toByteArray( RandomAccessibleInterval< ByteType > image ) {
		byte[] outputArray = new byte[ ( int ) Intervals.numElements( image ) ];
		long[] shape = Intervals.dimensionsAsLongArray( image );
		copyFromTo( image, ArrayImgs.bytes( outputArray, shape ) );
		return outputArray;
	}

	public static int[] toIntArray( RandomAccessibleInterval< IntType > image ) {
		int[] outputArray = new int[ ( int ) Intervals.numElements( image ) ];
		long[] shape = Intervals.dimensionsAsLongArray( image );
		copyFromTo( image, ArrayImgs.ints( outputArray, shape ) );
		return outputArray;
	}

	public static float[] toFloatArray( RandomAccessibleInterval< FloatType > image ) {
		float[] outputArray = new float[ ( int ) Intervals.numElements( image ) ];
		long[] shape = Intervals.dimensionsAsLongArray( image );
		copyFromTo( image, ArrayImgs.floats( outputArray, shape ) );
		return outputArray;
	}

	public static double[] toDoubleArray( RandomAccessibleInterval< DoubleType > image ) {
		double[] outputArray = new double[ ( int ) Intervals.numElements( image ) ];
		long[] shape = Intervals.dimensionsAsLongArray( image );
		copyFromTo( image, ArrayImgs.doubles( outputArray, shape ) );
		return outputArray;
	}

	private static < T extends Type< T > > void copyFromTo(
			RandomAccessibleInterval< T > source,
			RandomAccessibleInterval< T > destination ) {
		LoopBuilder.setImages( source, destination ).forEachPixel( ( i, o ) -> o.set( i ) );
	}

}
