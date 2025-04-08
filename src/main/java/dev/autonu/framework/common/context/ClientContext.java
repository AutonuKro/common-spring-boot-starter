package dev.autonu.framework.common.context;

import dev.autonu.framework.common.model.ClientUserAssociation;
import org.springframework.lang.Nullable;

/**
 * @author autonu2X
 */
final class ClientContext {

    static final ThreadLocal<ClientUserAssociation> TENANT_CONTEXT = new ThreadLocal<>();

    private ClientContext() {
    }

    static void set(ClientUserAssociation clientUserAssociation) {
        TENANT_CONTEXT.set(clientUserAssociation);
    }

    @Nullable
    static ClientUserAssociation get() {
        return TENANT_CONTEXT.get();
    }

    static void clear() {
        TENANT_CONTEXT.remove();
    }
}
