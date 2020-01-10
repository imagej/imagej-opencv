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

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
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
	public static < T > Mat toMat(
			final RandomAccessibleInterval< T > image ) {
		if (image.numDimensions() > 3)
			throw new IllegalArgumentException( "Images with more than 3 dimensions are not supported yet");
		final T type = Util.getTypeFromInterval( image );
		if ( type instanceof ByteType ) {
			@SuppressWarnings( "unchecked" )
			final RandomAccessibleInterval< ByteType > typedImage =
					( RandomAccessibleInterval< ByteType > ) image;
			return matByte( typedImage );
		}
		if ( type instanceof IntType ) {
			@SuppressWarnings( "unchecked" )
			final RandomAccessibleInterval< IntType > typedImage =
					( RandomAccessibleInterval< IntType > ) image;
			return matInt( typedImage );
		}
		if ( type instanceof FloatType ) {
			@SuppressWarnings( "unchecked" )
			final RandomAccessibleInterval< FloatType > typedImage =
					( RandomAccessibleInterval< FloatType > ) image;
			return matFloat( typedImage );
		}
		if ( type instanceof DoubleType ) {
			@SuppressWarnings( "unchecked" )
			final RandomAccessibleInterval< DoubleType > typedImage =
					( RandomAccessibleInterval< DoubleType > ) image;
			return matDouble( typedImage );
		}
		throw new IllegalArgumentException( "Unsupported image type: " + type.getClass().getName() );
	}

	/**
	 * Creates an OpenCV Mat matrix containing data from the given byte image.
	 * 
	 * @param image
	 *            The image which should be put into the Mat.
	 * @return A Mat containing the data of the image.
	 */
	public static Mat matByte(
			final RandomAccessibleInterval< ByteType > image ) {
		int[] shape = getImageShape( image );
		byte[] data = byteArray( image );
		return new Mat( shape.length, shape, CvType.CV_8UC1, new BytePointer( data ) );
	}

	/**
	 * Creates an OpenCV Mat matrix containing data from the given int image.
	 * 
	 * @param image
	 *            The image which should be put into the Mat.
	 * @return A Mat containing the data of the image.
	 */
	public static Mat matInt(
			final RandomAccessibleInterval< IntType > image ) {
		int[] shape = getImageShape( image );
		int[] data = intArray( image );
		return new Mat( shape.length, shape, CvType.CV_32SC1, new IntPointer( data ) );
	}

	/**
	 * Creates an OpenCV Mat matrix containing data from the given float image.
	 * 
	 * @param image
	 *            The image which should be put into the Mat.
	 * @return A Mat containing the data of the image.
	 */
	public static Mat matFloat(
			final RandomAccessibleInterval< FloatType > image ) {
		int[] shape = getImageShape( image );
		float[] data = floatArray( image );
		return new Mat( shape.length, shape, CvType.CV_32FC1, new FloatPointer( data ) );
	}

	/**
	 * Creates an OpenCV Mat matrix containing data from the given double image.
	 * 
	 * @param image
	 *            The image which should be put into the Mat.
	 * @return A Mat containing the data of the image.
	 */
	public static Mat matDouble(
			final RandomAccessibleInterval< DoubleType > image ) {
		int[] shape = getImageShape( image );
		double[] data = doubleArray( image );
		return new Mat( shape.length, shape, CvType.CV_64FC1, new DoublePointer( data ) );
	}

	public static byte[] byteArray(
			final RandomAccessibleInterval< ByteType > image ) {
		final byte[] array = extractByteArray( image );
		return array == null ? createByteArray( image ) : array;
	}

	public static int[] intArray(
			final RandomAccessibleInterval< IntType > image ) {
		final int[] array = extractIntArray( image );
		return array == null ? createIntArray( image ) : array;
	}

	public static float[] floatArray(
			final RandomAccessibleInterval< FloatType > image ) {
		final float[] array = extractFloatArray( image );
		return array == null ? createFloatArray( image ) : array;
	}

	public static double[] doubleArray(
			final RandomAccessibleInterval< DoubleType > image ) {
		final double[] array = extractDoubleArray( image );
		return array == null ? createDoubleArray( image ) : array;
	}

	public static byte[] createByteArray(
			final RandomAccessibleInterval< ByteType > image ) {
		final long[] dims = Intervals.dimensionsAsLongArray( image );
		final ArrayImg< ByteType, ByteArray > dest = ArrayImgs.bytes( dims );
		copy( image, dest );
		return dest.update( null ).getCurrentStorageArray();
	}

	public static int[] createIntArray(
			final RandomAccessibleInterval< IntType > image ) {
		final long[] dims = Intervals.dimensionsAsLongArray( image );
		final ArrayImg< IntType, IntArray > dest = ArrayImgs.ints( dims );
		copy( image, dest );
		return dest.update( null ).getCurrentStorageArray();
	}

	public static float[] createFloatArray(
			final RandomAccessibleInterval< FloatType > image ) {
		final long[] dims = Intervals.dimensionsAsLongArray( image );
		final ArrayImg< FloatType, FloatArray > dest = ArrayImgs.floats( dims );
		copy( image, dest );
		return dest.update( null ).getCurrentStorageArray();
	}

	public static double[] createDoubleArray(
			final RandomAccessibleInterval< DoubleType > image ) {
		final long[] dims = Intervals.dimensionsAsLongArray( image );
		final ArrayImg< DoubleType, DoubleArray > dest = ArrayImgs.doubles( dims );
		copy( image, dest );
		return dest.update( null ).getCurrentStorageArray();
	}

	public static byte[] extractByteArray(
			final RandomAccessibleInterval< ByteType > image ) {
		if ( !( image instanceof ArrayImg ) ) return null;
		@SuppressWarnings( "unchecked" )
		final ArrayImg< ByteType, ? > arrayImg = ( ArrayImg< ByteType, ? > ) image;
		final Object dataAccess = arrayImg.update( null );
		return dataAccess instanceof ByteArray ? //
				( ( ByteArray ) dataAccess ).getCurrentStorageArray() : null;
	}

	public static int[] extractIntArray(
			final RandomAccessibleInterval< IntType > image ) {
		if ( !( image instanceof ArrayImg ) ) return null;
		@SuppressWarnings( "unchecked" )
		final ArrayImg< IntType, ? > arrayImg = ( ArrayImg< IntType, ? > ) image;
		final Object dataAccess = arrayImg.update( null );
		return dataAccess instanceof IntArray ? //
				( ( IntArray ) dataAccess ).getCurrentStorageArray() : null;
	}

	public static float[] extractFloatArray(
			final RandomAccessibleInterval< FloatType > image ) {
		if ( !( image instanceof ArrayImg ) ) return null;
		@SuppressWarnings( "unchecked" )
		final ArrayImg< FloatType, ? > arrayImg = ( ArrayImg< FloatType, ? > ) image;
		final Object dataAccess = arrayImg.update( null );
		return dataAccess instanceof FloatArray ? //
				( ( FloatArray ) dataAccess ).getCurrentStorageArray() : null;
	}

	public static double[] extractDoubleArray(
			final RandomAccessibleInterval< DoubleType > image ) {
		if ( !( image instanceof ArrayImg ) ) return null;
		@SuppressWarnings( "unchecked" )
		final ArrayImg< DoubleType, ? > arrayImg = ( ArrayImg< DoubleType, ? > ) image;
		final Object dataAccess = arrayImg.update( null );
		return dataAccess instanceof DoubleArray ? //
				( ( DoubleArray ) dataAccess ).getCurrentStorageArray() : null;
	}

	public static < T extends RealType< T > > void copy(
			final RandomAccessibleInterval< T > source,
			final IterableInterval< T > dest ) {
		final RandomAccess< T > sourceAccess = source.randomAccess();
		final Cursor< T > destCursor = dest.localizingCursor();
		while ( destCursor.hasNext() ) {
			destCursor.fwd();
			sourceAccess.setPosition( destCursor );
			destCursor.get().set( sourceAccess.get() );
		}
	}

	public static int[] getImageShape( final RandomAccessibleInterval< ? > image ) {
		int n = image.numDimensions();
		int[] shape = new int[ n ];
		for ( int i = 0; i < n; i++ )
			shape[ i ] = ( int ) image.dimension( i );
		return shape;
	}

	public static long[] getMatShape( Mat mat ) {
		final long[] dims = new long[ mat.dims() ];
		for ( int i = 0; i < mat.dims(); i++ ) {
			dims[ i ] = mat.size( i );
		}
		return dims;
	}

}
