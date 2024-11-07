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
			RandomAccessibleInterval< T > ijSlice = Views.hyperSlice( img, dims.length - 1, i);
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
