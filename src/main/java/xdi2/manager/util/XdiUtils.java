package xdi2.manager.util;

import java.io.StringReader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.features.signatures.RSASignature;
import xdi2.core.features.signatures.Signatures;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.XDIReader;
import xdi2.core.io.XDIReaderRegistry;
import xdi2.core.security.signature.create.RSAStaticPrivateKeySignatureCreator;
import xdi2.core.syntax.XDIAddress;

public class XdiUtils {
	private static final Logger log = LoggerFactory.getLogger(XdiUtils.class);

	public static String normalizeCloudName (String cloudName) {
		if (! (cloudName.startsWith("=") || cloudName.startsWith("*") || cloudName.startsWith("+")) ) {
			return "=" + cloudName;
		}
		return cloudName;
	}

	public static XDIAddress convertIdToXdiAddress(String id) {
		return XDIAddress.create(StringUtils.newStringUtf8(Base64.decodeBase64(id)));
	}

	public static String convertXdiAddressToId(String xdiAddress) {
		return Base64.encodeBase64String(xdiAddress.getBytes());
	}

	public static String convertXdiAddressToId(XDIAddress xdiAddress) {
		return convertXdiAddressToId(xdiAddress.toString());
	}

	public static Graph signMessage(String message, XDIAddress[] addresses, String key) {

		try {
			// parse the message to graph
			Graph graph = MemoryGraphFactory.getInstance().openGraph();
			XDIReader xdiReader = XDIReaderRegistry.getAuto();
			xdiReader.read(graph, new StringReader(message));

			// find the addresses
			for (XDIAddress address : addresses) {

				ContextNode contextNode = graph.getDeepContextNode(address, true);
				if (contextNode == null) throw new RuntimeException("No context node found at address " + address);

				// sign
				RSASignature signature = (RSASignature) Signatures.createSignature(contextNode, "sha", 256, "rsa", 2048, true);

				PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(key));
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				Key k = keyFactory.generatePrivate(keySpec);

				new RSAStaticPrivateKeySignatureCreator((PrivateKey) k).createSignature(signature);
			}

			return graph;
		}
		catch(Exception e) {
			log.warn("Error while signing a message:\n" + message, e);
		}

		return null;

	}
}
