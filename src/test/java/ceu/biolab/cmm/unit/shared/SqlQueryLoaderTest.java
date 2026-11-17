package ceu.biolab.cmm.unit.shared;

import ceu.biolab.cmm.shared.repository.SqlQueryLoader;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SqlQueryLoaderTest {
    @Test
    void readsThroughStreamClosesItAndRetriesFailure() throws Exception {
        ResourceLoader resources = mock(ResourceLoader.class);
        Resource resource = mock(Resource.class);
        var stream = spy(new ByteArrayInputStream("SELECT 1".getBytes(StandardCharsets.UTF_8)));
        when(resources.getResource("classpath:query.sql")).thenReturn(resource);
        when(resource.getInputStream()).thenThrow(new IOException("unavailable")).thenReturn(stream);
        SqlQueryLoader loader = new SqlQueryLoader(resources);
        assertThrows(IOException.class, () -> loader.load("classpath:query.sql"));
        assertEquals("SELECT 1", loader.load("classpath:query.sql"));
        assertEquals("SELECT 1", loader.load("classpath:query.sql"));
        verify(resource, times(2)).getInputStream();
        verify(stream).close();
        verify(resource, never()).getFile();
    }
}
