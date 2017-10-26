package uk.gov.bis.lite.permissions.spire.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.api.view.Status;
import uk.gov.bis.lite.permissions.spire.model.SpireLicence;

import java.util.Collections;

public class SpireLicenceAdapterTest {

  @Test
  public void validSpireLicenceTest() throws Exception {
    SpireLicence spireLicence = new SpireLicence()
        .setSarId("SAR-123")
        .setSiteId("SITE-123")
        .setType("Type")
        .setSubType("Sub type")
        .setStatus("ACTIVE")
        .setReference("Reference")
        .setOriginalApplicationReference("OAREF")
        .setExporterApplicationReference("EAREF")
        .setIssueDate("31/12/2000")
        .setExpiryDate("31/12/2020")
        .setCountryList(ImmutableList.of("UK"))
        .setExternalDocumentUrl("http://www.example.org");

    LicenceView licence = SpireLicenceAdapter.adapt(spireLicence);

    assertThat(licence.getSarId()).isEqualTo("SAR-123");
    assertThat(licence.getSiteId()).isEqualTo("SITE-123");
    assertThat(licence.getType()).isEqualTo("Type");
    assertThat(licence.getSubType()).isEqualTo("Sub type");
    assertThat(licence.getStatus()).isEqualTo(Status.ACTIVE);
    assertThat(licence.getReference()).isEqualTo("Reference");
    assertThat(licence.getOriginalApplicationReference()).isEqualTo("OAREF");
    assertThat(licence.getExporterApplicationReference()).isEqualTo("EAREF");
    assertThat(licence.getIssueDate()).isEqualTo("2000-12-31");
    assertThat(licence.getExpiryDate()).isEqualTo("2020-12-31");
    assertThat(licence.getCountryList()).containsOnly("UK");
    assertThat(licence.getExternalDocumentUrl()).isEqualTo("http://www.example.org");
  }

  @Test
  public void emptySpireLicenceTest() throws Exception {
    SpireLicence spireLicence = new SpireLicence()
        .setSarId("")
        .setSiteId("")
        .setType("")
        .setSubType("")
        .setStatus("")
        .setReference("")
        .setOriginalApplicationReference("")
        .setExporterApplicationReference("")
        .setIssueDate("")
        .setExpiryDate("")
        .setCountryList(Collections.emptyList())
        .setExternalDocumentUrl("");

    LicenceView licence = SpireLicenceAdapter.adapt(spireLicence);

    assertThat(licence.getSarId()).isEmpty();
    assertThat(licence.getSiteId()).isEmpty();
    assertThat(licence.getType()).isEmpty();
    assertThat(licence.getSubType()).isEmpty();
    assertThat(licence.getStatus()).isNull();
    assertThat(licence.getReference()).isEmpty();
    assertThat(licence.getOriginalApplicationReference()).isEmpty();
    assertThat(licence.getExporterApplicationReference()).isEmpty();
    assertThat(licence.getIssueDate()).isNull();
    assertThat(licence.getExpiryDate()).isNull();
    assertThat(licence.getCountryList()).isEmpty();
    assertThat(licence.getExternalDocumentUrl()).isEmpty();
  }

  @Test
  public void nullSpireLicenceTest() throws Exception {
    SpireLicence spireLicence = new SpireLicence()
        .setSarId(null)
        .setSiteId(null)
        .setType(null)
        .setSubType(null)
        .setStatus(null)
        .setReference(null)
        .setOriginalApplicationReference(null)
        .setExporterApplicationReference(null)
        .setIssueDate(null)
        .setExpiryDate(null)
        .setCountryList(null)
        .setExternalDocumentUrl(null);

    LicenceView licence = SpireLicenceAdapter.adapt(spireLicence);

    assertThat(licence.getSarId()).isNull();
    assertThat(licence.getSiteId()).isNull();
    assertThat(licence.getType()).isNull();
    assertThat(licence.getSubType()).isNull();
    assertThat(licence.getStatus()).isNull();
    assertThat(licence.getReference()).isNull();
    assertThat(licence.getOriginalApplicationReference()).isNull();
    assertThat(licence.getExporterApplicationReference()).isNull();
    assertThat(licence.getIssueDate()).isNull();
    assertThat(licence.getExpiryDate()).isNull();
    assertThat(licence.getCountryList()).isNull();
    assertThat(licence.getExternalDocumentUrl()).isNull();
  }

  @Test
  public void statusTest() throws Exception {
    assertThatThrownBy(() -> SpireLicenceAdapter.adapt(new SpireLicence().setStatus("foo")))
        .isExactlyInstanceOf(SpireLicenceAdapterException.class)
        .hasMessageContaining("Unknown status");

    assertThat(SpireLicenceAdapter.adapt(new SpireLicence().setStatus("")).getStatus()).isNull();

    assertThat(SpireLicenceAdapter.adapt(new SpireLicence().setStatus("")).getStatus()).isNull();
  }

  @Test
  public void parseSpireDateTest() throws Exception {
    assertThat(SpireLicenceAdapter.parseSpireDate("01/01/2000")).isEqualTo("2000-01-01");

    assertThat(SpireLicenceAdapter.parseSpireDate("31/12/1999")).isEqualTo("1999-12-31");

    assertThat(SpireLicenceAdapter.parseSpireDate(null)).isNull();

    assertThat(SpireLicenceAdapter.parseSpireDate("")).isNull();

    assertThatThrownBy(() -> SpireLicenceAdapter.parseSpireDate("2000-01-01"))
        .isExactlyInstanceOf(SpireLicenceAdapterException.class)
        .hasMessageContaining("Unexpected date format");

    assertThatThrownBy(() -> SpireLicenceAdapter.parseSpireDate("some junk text"))
        .isExactlyInstanceOf(SpireLicenceAdapterException.class)
        .hasMessageContaining("Unexpected date format");
  }
}