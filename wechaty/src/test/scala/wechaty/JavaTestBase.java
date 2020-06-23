package wechaty;

import org.grpcmock.GrpcMock;
import static org.grpcmock.GrpcMock.grpcMock;
import org.junit.jupiter.api.BeforeAll;

/**
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2020-06-23
 */
public class JavaTestBase {
  @BeforeAll
  static void createServer() {
    GrpcMock.configureFor(grpcMock(0).build().start());
  }
}
