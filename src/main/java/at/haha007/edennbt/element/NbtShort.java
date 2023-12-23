package at.haha007.edennbt.element;

import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NbtShort extends NbtElement {
    private short value = 0;
}
