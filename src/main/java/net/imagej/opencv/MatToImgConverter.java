package net.imagej.opencv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.bytedeco.opencv.opencv_core.Mat;
import org.opencv.core.CvType;
import org.scijava.Prioritized;
import org.scijava.Priority;
import org.scijava.convert.AbstractConverter;
import org.scijava.convert.Converter;
import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * @author G.Turek for OpenCV version 4.1.2
 *         Only 3 dimensional, 1 channel images are supported for the moment
 */

@SuppressWarnings( "rawtypes" )
@Plugin( type = Converter.class, priority = Priority.LOW )
public class MatToImgConverter extends AbstractConverter< Mat, Img > {

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
		Mat imp = ( Mat ) o;
		return ( T ) toImg( imp );

	}

	public static RandomAccessibleInterval< ? > toImg( Mat mat ) {
		int type = mat.depth();

		if ( mat.channels() > 1 )
			throw new UnsupportedOperationException( "Only 1 channel images are currently supported" );

		switch ( type ) {
		case CvType.CV_8U:
			return toUnsignedByteImg( mat );
		case CvType.CV_8S:
			return toByteImg( mat );
		case CvType.CV_32S:
			return toIntImg( mat );
		case CvType.CV_32F:
			return toFloatImg( mat );
		case CvType.CV_64F:
			return tpDoubleImg( mat );
		default:
			throw new UnsupportedOperationException( "Unsupported CvType value: " + type );
		}
	}

	@Override
	public Class< Img > getOutputType() {
		return Img.class;
	}

	@Override
	public Class< Mat > getInputType() {
		return Mat.class;
	}

	/**
	 * Creates an image of type {@link UnsignedByteType} containing the data of an
	 * OpenCV Mat matrix with the data type {@link CvType#CV_8UC1}.
	 * 
	 * @param matrix
	 *            Mat.
	 * @return An image containing the data of the Mat.
	 */
	public static RandomAccessibleInterval< UnsignedByteType > toUnsignedByteImg( final Mat mat ) {
		final byte[] out = toByteArray( mat );
		final long[] dims = getMatShape( mat );
		long[] reshaped = dims.clone();   
		reshaped[0] = dims[1];
		reshaped[1] = dims[0];
		return ArrayImgs.unsignedBytes( out, reshaped );
	}
	
	/**
	 * Creates an image of type {@link ByteType} containing the data of an
	 * OpenCV Mat matrix with the data type {@link CvType#CV_8SC1}.
	 * 
	 * @param matrix
	 *            Mat.
	 * @return An image containing the data of the Mat.
	 */
	public static RandomAccessibleInterval< ByteType > toByteImg( final Mat mat ) {
		final byte[] out = toByteArray( mat );
		final long[] dims = getMatShape( mat );
		long[] reshaped = dims.clone();   
		reshaped[0] = dims[1];
		reshaped[1] = dims[0];
		return ArrayImgs.bytes( out, reshaped );
	}

	/**
	 * Creates an image of type {@link IntType} containing the data of an
	 * OpenCV Mat matrix with the data type {@link CvType#CV_32SC1}.
	 * 
	 * @param matrix
	 *            Mat.
	 * @return An image containing the data of the Mat.
	 */
	public static RandomAccessibleInterval< IntType > toIntImg( final Mat mat ) {
		final int[] out = toIntArray( mat );
		final long[] dims = getMatShape( mat );
		long[] reshaped = dims.clone();   
		reshaped[0] = dims[1];
		reshaped[1] = dims[0];
		return ArrayImgs.ints( out, reshaped );
	}

	/**
	 * Creates an image of type {@link FloatType} containing the data of an
	 * OpenCV Mat matrix with the data type {@link CvType#CV_32FC1}.
	 * 
	 * @param matrix
	 *            Mat.
	 * @return An image containing the data of the Mat.
	 */
	public static RandomAccessibleInterval< FloatType > toFloatImg( final Mat mat ) {
		float[] out = toFloatArray( mat );
		final long[] dims = getMatShape( mat );
		long[] reshaped = dims.clone();   
		reshaped[0] = dims[1];
		reshaped[1] = dims[0];
		return ArrayImgs.floats( out, dims );
	}

	/**
	 * Creates an image of type {@link DoubleType} containing the data of a
	 * OpenCV Mat matrix with the data type {@link CvType#CV_64FC1}.
	 * 
	 * @param matrix
	 *            Mat.
	 * @return An image containing the data of the Mat.
	 */
	public static RandomAccessibleInterval< DoubleType > tpDoubleImg( Mat mat ) {
		final double[] out = toDoubleArray( mat );
		final long[] dims = getMatShape( mat );
		long[] reshaped = dims.clone();   
		reshaped[0] = dims[1];
		reshaped[1] = dims[0];
		return ArrayImgs.doubles( out, dims );
	}

	public static byte[] toByteArray( final Mat mat ) {
		final byte[] out = new byte[ ( int ) mat.arraySize() ];
		mat.arrayData().get( out );
		return out;
	}

	public static int[] toIntArray( final Mat mat ) {
		final byte[] bytes = toByteArray( mat );
		IntBuffer intBuf = ByteBuffer.wrap( bytes ).order( ByteOrder.nativeOrder() ).asIntBuffer();
		int[] out = new int[ intBuf.remaining() ];
		intBuf.get( out );
		return out;
	}

	public static float[] toFloatArray( final Mat mat ) {
		final byte[] bytes = toByteArray( mat );
		FloatBuffer floatBuf = ByteBuffer.wrap( bytes ).order( ByteOrder.nativeOrder() ).asFloatBuffer();
		float[] out = new float[ floatBuf.remaining() ];
		floatBuf.get( out );
		return out;
	}

	public static double[] toDoubleArray( Mat mat ) {
		final byte[] bytes = toByteArray( mat );
		DoubleBuffer doubleBuf = ByteBuffer.wrap( bytes ).order( ByteOrder.nativeOrder() ).asDoubleBuffer();
		double[] out = new double[ doubleBuf.remaining() ];
		doubleBuf.get( out );
		return out;
	}

	public static long[] getMatShape( Mat mat ) {
		final long[] dims = new long[ mat.dims() ];
		for ( int i = 0; i < mat.dims(); i++ ) {
			dims[ i ] = mat.size( i );
		}
		return dims;
	}
}
