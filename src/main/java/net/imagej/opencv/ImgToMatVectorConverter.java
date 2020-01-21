package net.imagej.opencv;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.scijava.Prioritized;
import org.scijava.Priority;
import org.scijava.convert.AbstractConverter;
import org.scijava.convert.Converter;
import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Only 3 dimensional, 1 channel images are supported here.
 * For 2 dimensions see {@link ImgToMatConverter}
 * 
 * @author G.Turek for OpenCV version 4.1.2
 */

@SuppressWarnings( "rawtypes" )
@Plugin( type = Converter.class, priority = Priority.LOW )
public class ImgToMatVectorConverter extends AbstractConverter< RandomAccessibleInterval, MatVector > {

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
		RandomAccessibleInterval< T > img = ( RandomAccessibleInterval< T > ) o;
		int[] dims = Intervals.dimensionsAsIntArray( img );
		if ( dims.length < 3 )
			throw new IllegalArgumentException( "Images with less than 3 dimensions are not supported here, use ImgToMatConverter" );
		MatVector matVector = new MatVector( dims[ 2 ] );
		ImgToMatConverter converter = new ImgToMatConverter();
		for ( int i = 0; i < dims[2]; i++ ) {
			RandomAccessibleInterval< T > ijSlice = Views.hyperSlice( img, dims[2]-1, i);
			Mat mSlice = converter.convert( ijSlice, Mat.class );
			matVector.put( i , mSlice);
		}
		return ( T ) matVector;
	}

	@Override
	public Class< MatVector > getOutputType() {
		return MatVector.class;
	}

	@Override
	public Class< RandomAccessibleInterval > getInputType() {
		return RandomAccessibleInterval.class;
	}

}
