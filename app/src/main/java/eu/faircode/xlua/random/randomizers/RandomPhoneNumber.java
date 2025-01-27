package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomPhoneNumber implements IRandomizerOld {
    //public static final String FORMAT = "%s(%s)%s-%s";

    @Override
    public boolean isSetting(String setting) { return getSettingName().equalsIgnoreCase(setting); }

    @Override
    public String getSettingName() {  return "unique.gsm.phone.number"; }

    @Override
    public String getName() {
        return "Phone Number";
    }

    @Override
    public String getID() {
        return "%phone_number%";
    }

    @Override
    public String generateString() {
        return new StringBuilder()
                .append(AREA_CODE.get(RandomGenerator.nextInt(0, AREA_CODE.size() - 1)))
                .append(RandomStringGenerator.generateRandomNumberString(3))
                .append(RandomStringGenerator.generateRandomNumberString(4))
                .toString();
    }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() {
        return getName();
    }

    public static final List<String> AREA_CODE = Arrays.asList(
            "205", "251", "256", "334", "659", "938", // Alabama
            "907", // Alaska
            "480", "520", "602", "623", "928", // Arizona
            "479", "501", "870", // Arkansas
            // California
            "209", "213", "279", "310", "323", "341", "350", "408", "415", "424", "442", "510",
            "530", "559", "562", "619", "626", "628", "650", "657", "661", "669", "707", "714",
            "747", "760", "805", "818", "820", "831", "840", "858", "909", "916", "925", "949", "951",
            // Colorado
            "303", "719", "720", "970", "983",
            "203", "475", "860", "959", // Connecticut
            "302", // Delaware
            // Florida
            "239", "305", "321", "352", "386", "407", "448", "561", "656", "689", "727", "754",
            "772", "786", "813", "850", "863", "904", "941", "954",
            // Georgia
            "229", "404", "470", "478", "678", "706", "762", "770", "912", "943",
            "808", // Hawaii
            "208", "986", // Idaho
            // Illinois
            "217", "224", "309", "312", "331", "447", "464", "618", "630", "708", "773", "779",
            "815", "847", "872",
            // Indiana
            "219", "260", "317", "463", "574", "765", "812", "930",
            "319", "515", "563", "641", "712", // Iowa
            "316", "620", "785", "913", // Kansas
            "270", "364", "502", "606", "859", // Kentucky
            "225", "318", "337", "504", "985", // Louisiana
            "207", // Maine
            "240", "301", "410", "443", "667", // Maryland
            // Massachusetts
            "339", "351", "413", "508", "617", "774", "781", "857", "978",
            // Michigan
            "231", "248", "269", "313", "517", "586", "616", "734", "810", "906", "947", "989",
            // Minnesota
            "218", "320", "507", "612", "651", "763", "952",
            "228", "601", "662", "769", // Mississippi
            // Missouri
            "314", "417", "557", "573", "636", "660", "816",
            "406", // Montana
            "308", "402", "531", // Nebraska
            "702", "725", "775", // Nevada
            "603", // New Hampshire
            // New Jersey
            "201", "551", "609", "640", "732", "848", "856", "862", "908", "973",
            "505", "575", // New Mexico
            // New York
            "212", "315", "332", "347", "363", "516", "518", "585", "607", "631", "646", "680",
            "716", "718", "838", "845", "914", "917", "929", "934",
            // North Carolina
            "252", "336", "472", "704", "743", "828", "910", "919", "980", "984",
            "701", // North Dakota
            // Ohio
            "216", "220", "234", "326", "330", "380", "419", "440", "513", "567", "614", "740", "937",
            // Oklahoma
            "405", "539", "572", "580", "918",
            "458", "503", "541", "971", // Oregon
            // Pennsylvania
            "215", "223", "267", "272", "412", "445", "484", "570", "582", "610", "717", "724",
            "814", "835", "878",
            "401", // Rhode Island
            // South Carolina
            "803", "839", "843", "854", "864",
            "605", // South Dakota
            // Tennessee
            "423", "615", "629", "731", "865", "901", "931",
            // Texas
            "210", "214", "254", "281", "325", "346", "361", "409", "430", "432", "469", "512",
            "682", "713", "726", "737", "806", "817", "830", "832", "903", "915", "936", "940",
            "945", "956", "972", "979",
            "385", "435", "801", // Utah
            "802", // Vermont
            // Virginia
            "276", "434", "540", "571", "703", "757", "804", "826", "948",
            // Washington
            "206", "253", "360", "425", "509", "564",
            "202", "771", // Washington, DC
            "304", "681", // West Virginia
            "262", "414", "534", "608", "715", "920", // Wisconsin
            "307"  // Wyoming
    );


}
