package net.imagej.opencv;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.opencv.opencv_core.MatVector;
import org.scijava.Prioritized;
import org.scijava.Priority;
import org.scijava.convert.AbstractConverter;
import org.scijava.convert.Converter;
import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

/**
 * Only 1 channel images are supported for the moment.
 * 
 * @author G.Turek for OpenCV version 4.1.2
 */

@SuppressWarnings( "rawtypes" )
@Plugin( type = Converter.class, priority = Priority.LOW )
public class MatVectorToImgConverter extends AbstractConverter< MatVector, RandomAccessibleInterval > {

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
		MatVector img = ( MatVector ) o;
		MatToImgConverter converter = new MatToImgConverter();
		List< T > ijImgs = new ArrayList< T >( ( int ) img.size() );
		for ( int i = 0; i < img.size(); i++ ) {
			ijImgs.add( ( T ) converter.convert( img.get( i ), type ) );
		}
		return ( T ) Views.stack( ( List< ? extends RandomAccessibleInterval< T > > ) ijImgs );
	}

	@Override
	public Class< MatVector > getInputType() {
		return MatVector.class;
	}

	@Override
	public Class< RandomAccessibleInterval > getOutputType() {
		return RandomAccessibleInterval.class;
	}

}
