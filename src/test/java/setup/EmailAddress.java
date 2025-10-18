package setup;

import lombok.Builder;

@Builder
public record EmailAddress(
        String address, 
        String personal) {
}
