package br.unicamp.fnjv.wasis.api.utils.api;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.List;
import java.util.stream.Collectors;

public class ApiMapper {

    private static final ModelMapper modelMapper;

    static {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    private ApiMapper() {

    }

    /**
     * Maps from <i>source</i> to <i>target</i> object.
     *
     * @param source - Source object to map from
     * @param target - Target object to map to
     *
     * @return target
     */
    public static <S, T> T map(S source, T target) {
        modelMapper.map(source, target);

        return target;
    }

    /**
     * Maps from <i>source</i> to a new object of <i>target</i> type of class.
     *
     * @param source      - Source object that will be mapped
     * @param targetClass - Type of class to map to
     *
     * @return new object of the <i>targetClass</i> type.
     */
    public static <S, T> T map(S source, Class<T> targetClass) {
        return modelMapper.map(source, targetClass);
    }

    /**
     * Maps from a list of the <i>source</i> to a list of <i>target</i> type of class.
     *
     * @param source      - Source list that will be mapped
     * @param targetClass - Type of class to map to
     *
     * @return list of mapped object with the <i>targetClass</i> type.
     */
    public static <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source
                .stream()
                .map(entity -> map(entity, targetClass))
                .collect(Collectors.toList());
    }

}