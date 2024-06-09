/*******************************************************************************
 * Copyright or © or Copr. Institut des Sciences du Mouvement 
 * (CNRS & Aix Marseille Université)
 * 
 * The DOCoMETER Software must be used with a real time data acquisition 
 * system marketed by ADwin (ADwin Pro and Gold, I and II) or an Arduino 
 * Uno. This software, created within the Institute of Movement Sciences, 
 * has been developed to facilitate their use by a "neophyte" public in the 
 * fields of industrial computing and electronics.  Students, researchers or 
 * engineers can configure this acquisition system in the best possible 
 * conditions so that it best meets their experimental needs. 
 * 
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 * 
 * Contributors:
 *  - Frank Buloup - frank.buloup@univ-amu.fr - initial API and implementation [25/03/2020]
 ******************************************************************************/
package fr.univamu.ism.docometre.export;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Output stream for writing ustar archive files (tar) compatible
 * with the specification in IEEE Std 1003.1-2001.
 *
 * @since 3.1
 */
public class TarOutputStream extends FilterOutputStream {

	private int byteswritten = 0;
	private int datapos = 0;
	private long cursize = 0;

	/**
	 * Creates a new tar output stream.
	 *
	 * @param out the stream to write to
	 */
	public TarOutputStream(OutputStream out) {
		super(out);
	}

	/**
	 * Close the output stream and write any necessary padding.
	 */
	@Override
	public void close() throws IOException {
		// Spec says to write 1024 bytes of zeros at the end.
		byte[] zeros = new byte[1024];
		cursize = 1024;
		write(zeros, 0, 1024);

		// Default block size for tar files is 10240, so we have to
		// pad the end of the file to be a multiple of this size.
		if((byteswritten % 10240) != 0) {
			int length = 10240 - (byteswritten % 10240);
			cursize = length;
			zeros = new byte[length];
			write(zeros, 0, length);
		}
		super.close();
	}

	/**
	 * Close the current entry in the tar file.  Must be called
	 * after each entry is completed.
	 *
	 * @throws IOException
	 */
	public void closeEntry() throws IOException {
		byte[] data = new byte[512];
		int len = 512 - datapos;
		if(len > 0 && datapos > 0) {
			cursize = len;
			write(data, 0, len);
		}
	}

	/**
	 *  The checksum of a tar file header is simply the sum of the bytes in
	 *  the header.
	 *
	 * @param header
	 * @return checksum
	 */
	private long headerChecksum(byte[] header) {
		long sum = 0;
		for(int i = 0; i < 512; i++) {
			sum += header[i] & 0xff;
		}
		return sum;
	}

	/**
	 * Adds an entry for a new file in the tar archive.
	 *
	 * @param e TarEntry describing the file
	 * @throws IOException
	 */
	public void putNextEntry(TarEntry e) throws IOException {
		byte[] header = new byte[512];
		String filename = e.getName();
		String prefix = null;
		int pos, i;

		/* Split filename into name and prefix if necessary. */
		byte[] filenameBytes = filename.getBytes(StandardCharsets.UTF_8);
		if (filenameBytes.length > 99) {
			int seppos = filename.lastIndexOf('/');
			if(seppos == -1) {
				throw new IOException("filename too long"); //$NON-NLS-1$
			}
			prefix = filename.substring(0, seppos);
			filename = filename.substring(seppos + 1);
			filenameBytes = filename.getBytes(StandardCharsets.UTF_8);
			if (filenameBytes.length > 99) {
				throw new IOException("filename too long"); //$NON-NLS-1$
			}
		}

		/* Filename. */
		pos = 0;
		System.arraycopy(filenameBytes, 0, header, 0, filenameBytes.length);
		pos += 100;

		/* File mode. */
		StringBuilder mode = new StringBuilder(Long.toOctalString(e.getMode()));
		while(mode.length() < 7) {
			mode.insert(0,'0');
		}
		for(i = 0; i < 7; i++) {
			header[pos + i] = (byte) mode.charAt(i);
		}
		pos += 8;

		/* UID. */
		header[pos] = '0';
		pos += 8;

		/* GID. */
		header[pos] = '0';
		pos += 8;

		/* Length of the file. */
		String length = Long.toOctalString(e.getSize());
		for(i = 0; i < length.length(); i++) {
			header[pos + i] = (byte) length.charAt(i);
		}
		pos += 12;

		/* mtime */
		String mtime = Long.toOctalString(e.getTime());
		for(i = 0; i < mtime.length(); i++) {
			header[pos + i] = (byte) mtime.charAt(i);
		}
		pos += 12;

		/* "Blank" out the checksum. */
		for(i = 0; i < 8; i++) {
			header[pos + i] = ' ';
		}
		pos += 8;

		/* Link flag. */
		header[pos] = (byte) e.getFileType();
		pos += 1;

		/* Link destination. */
		pos += 100;

		/* Add ustar header. */
		String ustar = "ustar 00"; //$NON-NLS-1$
		for(i = 0; i < ustar.length(); i++) {
			header[pos + i] = (byte) ustar.charAt(i);
		}
		header[pos + 5] = 0;
		pos += 8;

		/* Username. */
		String uname = "nobody"; //$NON-NLS-1$
		for(i = 0; i < uname.length(); i++) {
			header[pos + i] = (byte) uname.charAt(i);
		}
		pos += 32;


		/* Group name. */
		String gname = "nobody"; //$NON-NLS-1$
		for(i = 0; i < gname.length(); i++) {
			header[pos + i] = (byte) gname.charAt(i);
		}
		pos += 32;

		/* Device major. */
		pos += 8;

		/* Device minor. */
		pos += 8;

		/* File prefix. */
		if(prefix != null) {
			byte[] prefixBytes = prefix.getBytes(StandardCharsets.UTF_8);
			if (prefixBytes.length > 155) {
				throw new IOException("prefix too large"); //$NON-NLS-1$
			}
			System.arraycopy(prefixBytes, 0, header, pos, prefixBytes.length);
		}

		long sum = headerChecksum(header);
		pos = 100 + 8 + 8 + 8 + 12 + 12;
		String sumval = Long.toOctalString(sum);
		for(i = 0; i < sumval.length(); i++) {
			header[pos + i] = (byte) sumval.charAt(i);
		}

		cursize = 512;
		write(header, 0, 512);

		cursize = e.getSize();
	}

	/**
	 * Writes data for the current file into the archive.
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
		datapos = (datapos + len) % 512;
		byteswritten += len;
		cursize -= len;
		if(cursize < 0) {
			throw new IOException("too much data written for current file"); //$NON-NLS-1$
		}
	}
}
