package dev.findfirst.bookmarkit.service;

import dev.findfirst.bookmarkit.model.AcademicImage;
import dev.findfirst.bookmarkit.repository.elastic.AcademicImageRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageSearchService {

  private final AcademicImageRepository imageRepo;
  private final ElasticsearchOperations elasticsearchOperations;

  public Optional<AcademicImage> findById(String id) {
    return imageRepo.findByImageId(id);
  }

  public void findByEmbedding() {

    var query =
        new StringQuery(
            """
            {
            "script_score": {
              "query": {
                "match_all": {}
            },
            "script": {
                "source": "cosineSimilarity(params.queryVector, 'image_embedding')+1.0",
                "params": {
                    "queryVector": [-0.06304964423179626, -0.18691158294677734, -0.23215752840042114, -0.03915028274059296, -0.12958447635173798, -0.10203901678323746, 0.13505442440509796, -0.3090129792690277, -0.11416440457105637, -0.10420432686805725, -0.15847566723823547, -0.018229428678750992, -0.08493643999099731, -0.14288587868213654, 0.3260754346847534, 0.023328274488449097, -0.22763320803642273, 0.10161013156175613, -0.05943042039871216, 0.06541509926319122, -0.5292274951934814, 0.11460874229669571, 0.14857669174671173, -0.27052316069602966, 0.34645822644233704, 0.2137666642665863, 0.15000012516975403, 0.3597317337989807, -0.15917041897773743, -0.31422749161720276, 0.12268249690532684, -0.17495180666446686, 0.33689096570014954, -0.08945769816637039, -0.8836920857429504, -0.08449742943048477, -0.2683776021003723, 0.08556485921144485, -0.13246837258338928, -0.06658969819545746, -0.16983401775360107, 0.23724430799484253, -0.10806341469287872, -0.027549833059310913, 0.21860721707344055, 0.030236192047595978, -0.12704885005950928, -0.3808436095714569, 0.33764350414276123, 0.1840132623910904, 0.06157273054122925, -0.10332267731428146, -0.11398652195930481, 0.2820010781288147, 0.5947586894035339, -0.2799595892429352, -0.002110585570335388, 0.39581799507141113, -0.4027588367462158, -0.20118960738182068, -0.49456456303596497, -0.06540165096521378, 0.014721015468239784, 0.367004930973053, -0.011378303170204163, -0.18200160562992096, -0.16368763148784637, -0.41084957122802734, -0.28514620661735535, -0.20012158155441284, 0.11164005845785141, -0.20908695459365845, 0.12882179021835327, 0.1395680159330368, -0.06168551743030548, 0.38229912519454956, 0.16440613567829132, 0.17769144475460052, 0.08803178369998932, -0.15874597430229187, 0.09132274240255356, 0.2009737640619278, -0.20747539401054382, 0.18525667488574982, 0.2143278270959854, -0.1217821016907692, 0.07381181418895721, 0.11342428624629974, -0.15939977765083313, -0.017933182418346405, 0.16641798615455627, 0.11148256063461304, -0.567861795425415, 0.7285608649253845, -0.027065083384513855, -0.09942404925823212, -0.49067920446395874, 0.39927488565444946, -0.4975106716156006, -0.33445844054222107, 0.379564493894577, 0.561522364616394, 0.2014787793159485, 0.47357723116874695, -0.30547770857810974, -0.1326863020658493, -0.2450459599494934, -0.038445599377155304, -0.01980847306549549, 0.009427674114704132, -0.07913325726985931, -0.4258542060852051, -0.33031004667282104, -0.03563014790415764, -0.062463220208883286, -0.003954943269491196, -0.42455989122390747, 0.14476828277111053, -0.13982006907463074, -0.34033262729644775, -0.04143272712826729, -0.6973330974578857, -0.22480086982250214, -0.3301958441734314, -0.14996671676635742, -0.2488771677017212, 0.17868342995643616, -0.11363981664180756, -0.16244329512119293, -0.14591984450817108, -0.03777715191245079, -0.1885472983121872, -0.1434938907623291, 3.7371487617492676, -0.636378824710846, -0.14575713872909546, -0.10203973948955536, -0.3674713671207428, -0.14340431988239288, 0.005730990320444107, -0.39688771963119507, 0.1396094411611557, 0.027036340907216072, 0.31068819761276245, -0.6077768802642822, -0.16125598549842834, 0.12236636877059937, -0.3673994541168213, -0.07325246185064316, 0.39964574575424194, -0.3869110643863678, -0.36909401416778564, -0.3347097933292389, 0.6709315776824951, 0.10007523000240326, -0.18116138875484467, 0.15641078352928162, -0.29884421825408936, 0.06908448040485382, -0.032046277076005936, 0.4956567585468292, 0.09809209406375885, 0.29452812671661377, 0.2253771722316742, -0.3439459800720215, 0.017984140664339066, -0.06485407054424286, -0.28411561250686646, 0.154854416847229, -0.09969088435173035, 0.19749067723751068, -0.1626894623041153, 0.10319593548774719, -0.1852302849292755, 0.06474436819553375, 0.1707204282283783, -0.23178867995738983, -0.025613218545913696, 0.08845162391662598, 0.3099532127380371, -0.11608617007732391, -0.012513354420661926, -0.40902087092399597, -0.3227790594100952, 0.23805734515190125, -0.17499059438705444, 0.01939377188682556, 0.24154198169708252, -0.3070518970489502, 0.20156827569007874, -0.25926727056503296, -0.3324523866176605, -0.21077583730220795, 0.5277464985847473, 0.03490310162305832, 0.22747868299484253, 0.02974504977464676, 0.252523809671402, -0.36773136258125305, 0.11982554197311401, 0.015407315455377102, 0.213578999042511, 0.17769664525985718, 0.02225855365395546, -0.008816825225949287, -0.37341606616973877, 0.13608482480049133, -0.21580493450164795, -0.021420523524284363, -0.03956859931349754, -0.1795351356267929, 0.4543532729148865, 0.4581258296966553, 0.07694120705127716, -0.05575764179229736, 0.08665547519922256, -0.11142542958259583, 0.3879912197589874, 0.3126186728477478, 0.21647943556308746, -0.1709555983543396, -0.16109836101531982, -0.04509860649704933, -0.40494176745414734, -0.008991323411464691, -0.42213672399520874, 0.16923537850379944, 0.17878343164920807, 0.0832512229681015, 0.07590114325284958, 0.20734095573425293, 0.2503129839897156, 0.32092395424842834, 0.4345388114452362, 0.1433902382850647, 0.8212974071502686, 0.143449068069458, -0.2368156909942627, -0.13246256113052368, -0.5607414841651917, 0.09917007386684418, 0.19976834952831268, 0.1404740810394287, 0.4220024049282074, -0.010852279141545296, -0.03677237033843994, 0.10964325815439224, 0.28576523065567017, 0.04467954486608505, 0.5163648724555969, -0.05853091925382614, 0.2201474905014038, 0.10989408940076828, 0.2607274055480957, 0.0007307007908821106, -0.06392297893762589, -0.10822053998708725, -0.03772293031215668, -0.08968605101108551, 0.42697665095329285, 0.3576511740684509, 0.07904491573572159, 0.35143011808395386, 0.14277052879333496, 0.198897123336792, -0.20402181148529053, 0.15885283052921295, -0.37952324748039246, -0.039708562195301056, 0.22118467092514038, 0.2538689076900482, -0.15083549916744232, 0.45495596528053284, 0.38391926884651184, 0.20092721283435822, -0.36344408988952637, 0.22106647491455078, 0.12184673547744751, 0.19454729557037354, -0.06219625473022461, -0.15609827637672424, 0.1882389783859253, 0.3995380401611328, 0.2082889974117279, 0.07929608970880508, 0.2588111162185669, 0.21793192625045776, 0.02643711119890213, 0.07134051620960236, -0.19001929461956024, -0.2040591537952423, -0.12064225226640701, -0.4178948998451233, -0.21115480363368988, -0.17735949158668518, -0.15089716017246246, -0.4583593010902405, 0.39024996757507324, -0.15871404111385345, 0.19018620252609253, -0.5084985494613647, 0.28073397278785706, -0.07116087526082993, 0.02862553671002388, -0.0717928558588028, -0.47476547956466675, 0.23911848664283752, 0.21530047059059143, 0.1206115335226059, 0.3554270267486572, -0.029378917068243027, 0.6624347567558289, 3.731886148452759, 0.19459059834480286, -0.043292175978422165, 0.04767085239291191, 0.29774561524391174, 0.18542417883872986, -0.2257564663887024, -0.0360046848654747, 0.38387876749038696, 0.3344544470310211, -0.06436549127101898, -0.020788440480828285, 0.19100868701934814, 0.2115403413772583, 0.09001243114471436, -0.13072971999645233, 0.2432493418455124, -0.3996124565601349, -0.269802987575531, -0.050125330686569214, 0.6285575032234192, 0.17387354373931885, 0.2851825952529907, 0.07979872822761536, 0.26323795318603516, -0.06519423425197601, -0.09651277959346771, -0.25066253542900085, 0.40801072120666504, 0.252464234828949, 0.10444694757461548, -0.5775589942932129, 0.12575829029083252, 0.04070660471916199, 0.21292179822921753, -0.23933178186416626, -0.2342645823955536, -0.0962069183588028, 0.33545899391174316, 0.10355784744024277, 0.09110040217638016, 0.13842415809631348, -0.36655980348587036, 0.14040178060531616, 0.3177485466003418, -0.2779305875301361, 0.01009763777256012, -0.15674208104610443, 0.660171389579773, -0.3459276258945465, -0.42799243330955505, 0.352944552898407, -0.02356088161468506, 0.20220406353473663, 0.016695089638233185, 0.32959169149398804, -0.13404494524002075, 0.24669602513313293, 0.12153469771146774, -0.157576784491539, -0.07786834239959717, 0.22573482990264893, 0.11985784769058228, 0.24495302140712738, 0.1083691194653511, -0.2319951355457306, 0.279979407787323, 0.2248012125492096, 0.05677815526723862, 0.07930859923362732, -0.17429673671722412, -0.012820832431316376, -0.2747761011123657, -0.17150068283081055, -0.6006874442100525, -0.13669411838054657, 0.014542728662490845, -0.3202855587005615, -0.7082923054695129, -0.12632623314857483, 0.38856980204582214, -0.006189137697219849, 0.3251440227031708, 0.05545617640018463, -0.05159197002649307, 0.029907554388046265, -0.16867990791797638, 0.6204671263694763, 0.1732412427663803, -0.23223859071731567, 0.10718682408332825, 0.388791561126709, -0.08389734476804733, 0.02797849476337433, -0.03097354993224144, -0.010372385382652283, 0.27078959345817566, 0.5220348238945007, 0.7854844927787781, -0.14497053623199463, 0.09985796362161636, -0.17821183800697327, 0.17636814713478088, 0.4287707209587097, -0.05036293715238571, -0.16396844387054443, 0.3176366090774536, -0.043779149651527405, 0.8162893056869507, -0.04325070232152939, -0.11182952672243118, -0.5603313446044922, 0.17830702662467957, 0.21234838664531708, 0.11669433116912842, 0.22520768642425537, 0.35109102725982666, -0.21487702429294586, -0.11745868623256683, -0.10632634162902832, -0.17929768562316895, 0.14438863098621368, 0.17698785662651062, -0.20456427335739136, -0.6198019981384277, 0.5880913138389587, -0.028255678713321686, -0.11786765605211258, 0.0801914781332016, 0.3287591338157654, -0.13438908755779266, -0.0944739505648613, -0.06200215220451355, 0.29361972212791443, -0.07775945216417313, 0.0005256310105323792, 0.13395169377326965, 0.075734943151474, -0.04291738569736481, 0.2283027172088623, 0.41652846336364746, 0.24757465720176697, -0.33489200472831726, -0.5247402787208557, -0.10329625755548477, -0.07725369930267334, 0.2680397927761078, 0.1222730278968811, 0.3630189001560211, -0.2036750614643097, 0.12776488065719604, 0.1723831743001938, 0.20956529676914215, 0.01904309168457985, -0.01090230792760849, 0.11422289907932281, -0.5069953203201294, -0.23094846308231354, 0.16781280934810638, -0.3095533847808838, 0.05121820420026779, 0.08234713971614838, 0.3643515408039093, -0.17514324188232422, 0.11320244520902634, 0.14084674417972565, 0.07564806938171387, 0.26897749304771423, -0.11190338432788849, 0.022996259853243828, 0.5234920382499695, -0.12444201111793518, -0.2305121123790741, -0.09767881035804749, -0.4424622058868408, -0.24884219467639923, 0.23274531960487366, 0.0808190107345581, 0.038651444017887115, -0.013174004852771759, -0.20956216752529144, 1.3982597589492798, -0.13366614282131195, 0.05712108314037323, 0.24777069687843323, 0.07982102036476135, -0.40287262201309204, 0.08007925748825073, 0.22101038694381714, -0.2142050564289093, 0.09546059370040894, 0.22978591918945312, -0.07128444314002991, 0.036431554704904556, -0.485047847032547, 0.41071924567222595, 0.12134420871734619, 0.13862435519695282, -0.09656783193349838, -0.4995706081390381]
                }
            }
        }
    }
        """);

    SearchHits<AcademicImage> searchHits =
        elasticsearchOperations.search(query, AcademicImage.class);
  }
}
